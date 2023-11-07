package org.homio.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingRunnable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.Status;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public interface EntityService<S extends EntityService.ServiceInstance>
    extends HasStatusAndMsg {

    ReentrantLock serviceAccessLock = new ReentrantLock();

    Context context();

    /**
     * Test where is able to initialize entity
     *
     * @return not configured errors
     */
    @JsonIgnore
    @Nullable Set<String> getConfigurationErrors();

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
    @NotNull Class<S> getEntityServiceItemClass();

    @SneakyThrows
    default @NotNull Optional<S> getOrCreateService(@NotNull Context context) {
        serviceAccessLock.lock();
        try {
            if (context().service().isHasEntityService(getEntityID())) {
                return Optional.of((S) context().service().getEntityService(getEntityID()));
            }
            try {
                S service = createService(context);
                if (service != null) {
                    context().service().addEntityService(getEntityID(), service);
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
    @Nullable S createService(@NotNull Context context);

    String getEntityID();

    default void destroyService(Exception ex) throws Exception {
        S service = (S) context().service().removeEntityService(getEntityID());
        if (service != null) {
            service.destroy(false, ex);
        }
    }

    interface WatchdogService {

        void restartService();

        @JsonIgnore
        String isRequireRestartService();
    }

    @Getter
    abstract class ServiceInstance<E extends EntityService<?>> implements WatchdogService {

        protected Logger log = LogManager.getLogger(getClass());
        protected final @NotNull @Accessors(fluent = true) Context context;
        protected final String entityID;
        private final AtomicBoolean initializing = new AtomicBoolean(false);
        protected E entity;
        protected long entityHashCode;
        protected long requestedEntityHashCode;

        public ServiceInstance(@NotNull Context context, @NotNull E entity, boolean fireFirstInitialize) {
            this.context = context;
            this.entityID = entity.getEntityID();
            this.entity = entity;

            if (fireFirstInitialize) {
                entityUpdated(entity);
            }
        }

        public void testServiceWithSetStatus() {
            try {
                entity.setStatus(Status.TESTING);
                testService();
                entity.setStatusOnline();
            } catch (Exception ex) {
                entity.setStatusError(ex);
            } finally {
                updateNotificationBlock();
            }
        }

        /**
         * Not used yet. Able to fire to backup data
         *
         * @param backupContext - context
         */
        public void backupData(BackupContext backupContext) {

        }

        /**
         * Fires to update entity inside in-memory service each time when entity fetched/updated testService() method calls always after this to check service
         * actual status
         *
         * @param newEntity - updated entity
         */
        public void entityUpdated(@NotNull E newEntity) {
            requestedEntityHashCode = getEntityHashCode(newEntity);
            entity = newEntity;

            if (requestedEntityHashCode != entityHashCode && !initializing.get()) {
                startInitialization();
            }
        }

        /**
         * Async restarting service fired by watchdog service if isRequireRestartService return not null. Restart service in interval 1..2 minutes Service
         * should be as fast as possible. Use inner async if possible Method calls in ForkJoin pool at same time with other services if need
         */
        @Override
        public void restartService() {
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

        public void updateNotificationBlock() {
        }

        /**
         * Executes +- every minute
         * @return Check if need restart service before call restartService().. Return restart reason or null if not require
         */
        @Override
        public String isRequireRestartService() {
            return null;
        }

        public abstract void destroy(boolean forRestart, @Nullable Exception ex) throws Exception;

        private synchronized void startInitialization() {
            initializing.set(true);
            entity.setStatus(Status.INITIALIZE);
            Set<String> errors = entity.getConfigurationErrors();
            String bgpEntityID = "init-" + entity.getEntityID();
            if (errors != null && !errors.isEmpty()) {
                context.bgp().cancelThread(bgpEntityID);
                String msg = errors.stream().map(Lang::getServerMessage).collect(Collectors.joining("<br>"));
                entity.setStatus(Status.ERROR, msg);
                initializing.set(false);
                return;
            }

            // deffer initialize to register service in map and avoid blocking. Also execute last updated entity
            context.bgp().builder(bgpEntityID).delay(Duration.ofSeconds(3))
                   .execute(() -> {
                       try {
                           log.info("[{}]: Start initialization of entity service: {}", entityID, entity.getTitle());
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

        protected void testService() {

        }

        protected void firstInitialize() {
            // fallback to initialize
            initialize();
        }

        protected abstract void initialize();

        private void fireWithSetStatus(ThrowingRunnable<Exception> handler) {
            try {
                handler.run();
            } catch (Exception ex) {
                entity.setStatusError(ex);
                log.error("[{}]: Unable to initialize service: {}", entityID, entity.getTitle());
            } finally {
                updateNotificationBlock();
            }
        }

        protected long getEntityHashCode(E entity) {
            return entity.getEntityServiceHashCode();
        }

        public interface BackupContext {

            Path getBackupPath();
        }
    }
}

