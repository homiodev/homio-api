package org.homio.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingRunnable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.homio.api.Context;
import org.homio.api.JSDisableMethod;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.Status;
import org.homio.api.model.endpoint.BaseDeviceEndpoint;
import org.homio.api.model.endpoint.DeviceEndpoint;
import org.homio.api.ui.UISidebarChildren;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityService<S extends EntityService.ServiceInstance> extends HasStatusAndMsg {

  @NotNull ReentrantLock serviceAccessLock = new ReentrantLock();

  @NotNull
  Context context();

  /**
   * Test where is able to initialize entity
   *
   * @return not configured errors
   */
  @JsonIgnore
  @Nullable
  default Set<String> getConfigurationErrors() {
    if (this instanceof BaseEntity be) {
      Set<String> missingMandatoryFields = be.getMissingMandatoryFields();
      if (!missingMandatoryFields.isEmpty()) {
        return missingMandatoryFields.stream()
            .map(f -> "W.ERROR.NO_" + f.toUpperCase())
            .collect(Collectors.toSet());
      }
    }
    return null;
  }

  /**
   * Able to check if need reinitialize entity service
   *
   * @return
   */
  @JsonIgnore
  long getEntityServiceHashCode();

  default boolean isStart() {
    return true; // not all entities has this functionality
  }

  /**
   * @return Get service or throw error if not found
   */
  @JsonIgnore
  default @NotNull S getService() throws NotFoundException {
    Object service = context().service().getEntityService(getEntityID());
    if (service == null) {
      throw new NotFoundException("Service for entity: " + getEntityID() + " not found");
    }
    return (S) service;
  }

  @JsonIgnore
  default @NotNull Optional<S> optService() {
    return Optional.ofNullable((S) context().service().getEntityService(getEntityID()));
  }

  @JsonIgnore
  @JSDisableMethod
  @NotNull
  Class<S> getEntityServiceItemClass();

  @SneakyThrows
  default @NotNull Optional<S> getOrCreateService(@NotNull Context context) {
    serviceAccessLock.lock();
    try {
      if (context().service().isHasService(getEntityID())) {
        return Optional.of((S) context().service().getEntityService(getEntityID()));
      }
      try {
        S service = createService(context);
        if (service != null) {
          context().service().addService(getEntityID(), service);
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
      return Optional.ofNullable((S) context().service().getEntityService(getEntityID()));
    } finally {
      serviceAccessLock.unlock();
    }
  }

  /**
   * Create service factory method
   *
   * @param context -
   * @return service or null if service has to be created during some external process
   */
  @JSDisableMethod
  @Nullable
  S createService(@NotNull Context context);

  String getEntityID();

  @JSDisableMethod
  default void destroyService(Exception ex) throws Exception {
    S service = (S) context().service().removeService(getEntityID());
    if (service != null) {
      service.destroy(false, ex);
    }
  }

  interface WatchdogService {

    @JSDisableMethod
    void restartService();

    @JsonIgnore
    String isRequireRestartService();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Getter
  abstract class ServiceInstance<E extends EntityService<?>>
      implements WatchdogService, BaseService {

    protected final @NotNull @Accessors(fluent = true) Context context;
    protected final @NotNull String entityID;
    private final @NotNull String name;
    private final @NotNull AtomicBoolean initializing = new AtomicBoolean(false);
    protected @NotNull Logger log = LogManager.getLogger(getClass());
    protected @NotNull E entity;
    protected long entityHashCode;
    protected long requestedEntityHashCode;
    private @Setter boolean exposeService;
    private @Setter String parent;

    public ServiceInstance(
        @NotNull Context context,
        @NotNull E entity,
        boolean fireFirstInitialize,
        @NotNull String name) {
      this(context, entity, fireFirstInitialize, name, false);
    }

    // name - use simple notification block if name not null
    public ServiceInstance(
        @NotNull Context context,
        @NotNull E entity,
        boolean fireFirstInitialize,
        @NotNull String name,
        boolean enableNotificationBlock) {
      this.context = context;
      this.entityID = entity.getEntityID();
      this.entity = entity;
      this.name = name;

      if (enableNotificationBlock && entity instanceof BaseEntity be) {
        context.ui().notification().updateBlock("app", be);

        context
            .event()
            .addEntityUpdateListener(
                entityID,
                "service",
                e -> {
                  context.ui().notification().updateBlock("app", be);
                  updateNotificationBlock();
                });
        context
            .event()
            .addEntityStatusUpdateListener(
                entityID,
                "service",
                e -> {
                  context.ui().notification().updateBlock("app", be);
                  updateNotificationBlock();
                });
      } else {
        context
            .event()
            .addEntityUpdateListener(entityID, "service", e -> updateNotificationBlock());
        context
            .event()
            .addEntityStatusUpdateListener(entityID, "service", e -> updateNotificationBlock());
      }

      if (fireFirstInitialize) {
        entityUpdated(entity);
      }
    }

    @Override
    public @Nullable String getIcon() {
      UISidebarChildren annotation =
          entity.getClass().getDeclaredAnnotation(UISidebarChildren.class);
      return annotation == null ? null : annotation.icon();
    }

    @Override
    public @Nullable String getColor() {
      UISidebarChildren annotation =
          entity.getClass().getDeclaredAnnotation(UISidebarChildren.class);
      return annotation == null ? null : annotation.color();
    }

    public void testServiceWithSetStatus() {
      try {
        entity.setStatus(Status.TESTING);
        testService();
        entity.setStatusOnline();
      } catch (Exception ex) {
        entity.setStatusError(ex);
      }
    }

    /**
     * Not used yet. Able to fire to backup data
     *
     * @param backupContext - context
     */
    public void backupData(BackupContext backupContext) {}

    /**
     * Fires to update entity inside in-memory service each time when entity fetched/updated
     * testService() method calls always after this to check service actual status
     *
     * @param newEntity - updated entity
     */
    @JSDisableMethod
    public void entityUpdated(@NotNull E newEntity) {
      requestedEntityHashCode = getEntityHashCode(newEntity);
      entity = newEntity;

      if (requestedEntityHashCode != entityHashCode && !initializing.get()) {
        startInitialization();
      }
      hideConfiguredEndpoints();
    }

    private void hideConfiguredEndpoints() {
      if (entity instanceof DeviceEndpointsBehaviourContract dc && entity.getStatus().isOnline()) {
        var deviceEndpoints = new ArrayList<>(dc.getDeviceEndpoints().values());
        for (DeviceEndpoint deviceEndpoint : deviceEndpoints) {
          if (deviceEndpoint instanceof BaseDeviceEndpoint generalDeviceEndpoint) {
            try {
              generalDeviceEndpoint.setDevice(dc);
            } catch (Exception ex) {
              log.error("Unable to set device to endpoint", ex);
            }
          }
        }
      }
    }

    public boolean isInternetRequiredForService() {
      return false;
    }

    /**
     * Async restarting service fired by watchdog service if isRequireRestartService return not
     * null. Restart service in interval 1..2 minutes Service should be as fast as possible. Use
     * inner async if possible Method calls in ForkJoin pool at same time with other services if
     * need
     */
    @Override
    @JSDisableMethod
    public void restartService() {
      if (isInternetRequiredForService() && !context.event().isInternetUp()) {
        // just skip restarting
        return;
      }
      // check entityHashCode with 0 to allow firstInitialization to be first called
      if (entityHashCode != 0 && initializing.compareAndSet(false, true)) {
        try {
          log.info("[{}]: Restarting entity service: {}", entityID, entity.getTitle());
          entity.setStatus(Status.RESTARTING);
          fireWithSetStatus(this::initialize);
        } finally {
          log.debug("[{}]: Done restarting entity service: {}", entityID, entity.getTitle());
          initializing.set(false);
        }
      }
    }

    @JSDisableMethod
    public void updateNotificationBlock() {}

    /**
     * Executes +- every minute
     *
     * @return Check if need restart service before call restartService().. Return restart reason or
     *     null if not require
     */
    @Override
    public String isRequireRestartService() {
      return null;
    }

    @JSDisableMethod
    public abstract void destroy(boolean forRestart, @Nullable Exception ex) throws Exception;

    private synchronized void startInitialization() {
      if (isInternetRequiredForService() && !context.event().isInternetUp()) {
        // just skip restarting
        return;
      }
      initializing.set(true);
      entity.setStatus(entity.isStart() ? Status.INITIALIZE : Status.CLOSING);
      Set<String> errors = entity.getConfigurationErrors();
      String bgpEntityID = "init-" + entity.getEntityID();
      if (errors != null && !errors.isEmpty()) {
        context.bgp().cancelThread(bgpEntityID);
        String msg =
            errors.stream().map(Lang::getServerMessage).collect(Collectors.joining("<br>"));
        entity.setStatus(Status.ERROR, msg);
        initializing.set(false);
        return;
      }

      // deffer initialize to register service in map and avoid blocking. Also execute last updated
      // entity
      context
          .bgp()
          .builder(bgpEntityID)
          .delay(Duration.ofSeconds(3))
          .execute(
              () -> {
                try {
                  log.info(
                      "[{}]: Start initialization of entity service: {}",
                      entityID,
                      entity.getTitle());
                  // delay update hashCode
                  if (entityHashCode == requestedEntityHashCode) {
                    return;
                  }
                  boolean firstInitialization = entityHashCode == 0;
                  entityHashCode = requestedEntityHashCode;
                  if (!firstInitialization) {
                    destroy(true, null);
                  }
                  if (!entity.isStart()) {
                    entity.setStatus(Status.OFFLINE);
                  } else {
                    if (firstInitialization) {
                      fireWithSetStatus(this::firstInitialize);
                    } else {
                      fireWithSetStatus(this::initialize);
                    }
                  }
                  if (entity instanceof BaseEntity be) {
                    context.ui().updateItem(be);
                  }
                } finally {
                  initializing.set(false);
                  // if new update
                  if (entityHashCode != requestedEntityHashCode) {
                    startInitialization();
                  }
                }
              });
    }

    protected void testService() {}

    protected void firstInitialize() {
      // fallback to initialize
      initialize();
    }

    protected abstract void initialize();

    private void fireWithSetStatus(ThrowingRunnable<Exception> handler) {
      try {
        handler.run();
        // if forget to change status
        if (entity.getStatus() == Status.INITIALIZE) {
          entity.setStatusOnline();
          hideConfiguredEndpoints();
        }
      } catch (Exception ex) {
        entity.setStatusError(ex);
        log.error("[{}]: Unable to initialize service: {}", entityID, entity.getTitle());
      }
    }

    protected long getEntityHashCode(E entity) {
      long code = entityID.hashCode();
      code += entity.isStart() ? 1 : 2;
      return code + entity.getEntityServiceHashCode();
    }

    public interface BackupContext {

      Path getBackupPath();
    }
  }
}
