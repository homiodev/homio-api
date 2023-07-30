package org.homio.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingRunnable;
import lombok.Getter;
import lombok.SneakyThrows;
import org.homio.api.EntityContext;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Configure service for entities. I.e. MongoEntity has MongoService which correspond for communications, RabbitMQ, etc...
 */
public interface EntityService<S extends EntityService.ServiceInstance, T extends HasEntityIdentifier>
        extends HasStatusAndMsg<T> {

    ReentrantLock serviceAccessLock = new ReentrantLock();

    Map<String, Object> entityToService = new ConcurrentHashMap<>();

    EntityContext getEntityContext();

    /**
     * @return Get service or throw error if not found
     */
    @JsonIgnore
    default @NotNull S getService() throws NotFoundException {
        Object service = entityToService.get(getEntityID());
        if (service == null) {
            throw new NotFoundException("Service for entity: " + getEntityID() + " not found");
        }
        return (S) service;
    }

    @JsonIgnore
    default @NotNull Optional<S> optService() {
        return Optional.ofNullable((S) entityToService.get(getEntityID()));
    }

    @JsonIgnore
    @NotNull Class<S> getEntityServiceItemClass();

    @SneakyThrows
    default @NotNull Optional<S> getOrCreateService(@NotNull EntityContext entityContext) {
        serviceAccessLock.lock();
        try {
            if (entityToService.containsKey(getEntityID())) {
                return Optional.of((S) entityToService.get(getEntityID()));
            }
            try {
                S service = createService(entityContext);
                if (service != null) {
                    entityToService.put(getEntityID(), service);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return Optional.ofNullable((S) entityToService.get(getEntityID()));
        } finally {
            serviceAccessLock.unlock();
        }
    }

    /**
     * Create service factory method
     *
     * @param entityContext -
     * @return service or null if service has to be created during some external process
     */
    @Nullable S createService(@NotNull EntityContext entityContext);

    String getEntityID();

    default void destroyService() throws Exception {
        S service = (S) entityToService.remove(getEntityID());
        if (service != null) {
            service.destroy();
        }
    }

    @Getter
    abstract class ServiceInstance<E extends EntityService<?, ?>> {

        protected final @NotNull EntityContext entityContext;
        protected String entityID;
        protected E entity;
        protected long entityHashCode;

        /**
         * Avoid to save any data to db during create service instance.
         *
         * @param entityContext ec
         */
        public ServiceInstance(@NotNull EntityContext entityContext) {
            this.entityContext = entityContext;
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
         * Fires to update entity inside in-memory service each time when entity fetched/updated testService() method calls always after this to check service
         * actual status
         *
         * @param newEntity - updated entity
         */
        public void entityUpdated(@NotNull E newEntity) {
            boolean firstSet = entity == null;
            entityID = newEntity.getEntityID();
            long newEntityHashCode = getEntityHashCode(newEntity);
            boolean requireReinitialize = entityHashCode != newEntityHashCode;
            entityHashCode = newEntityHashCode;
            entity = newEntity;

            if (firstSet) {
                entityContext.bgp().execute(() -> fireWithSetStatus(this::firstInitialize));
            } else if (requireReinitialize) {
                entityContext.bgp().execute(() -> fireWithSetStatus(this::initialize));
            }
        }

        /**
         * @return watchdog if service supports watchdog capabilities
         */
        public @Nullable WatchdogService getWatchdog() {
            return null;
        }

        protected boolean fireWithSetStatus(ThrowingRunnable<Exception> handler) {
            try {
                handler.run();
                return true;
            } catch (Exception ex) {
                entity.setStatusError(ex);
                return false;
            }
        }

        protected void testService() {

        }

        protected void firstInitialize() {
            // fallback to initialize
            initialize();

        }

        protected abstract void initialize();

        protected abstract long getEntityHashCode(E entity);

        protected void destroy() throws Exception {

        }
    }

    interface WatchdogService {
        /**
         * Restarting service fired by watchdog service if isWatchdogEnabled true. Restart service in interval 1..2 minutes
         * Service should be as fast as possible. Use inner async if possible
         * Method calls in ForkJoin pool at same time with other services if need
         */
        void restartService();

        /**
         * @return Check if need restart service before call restartService()..
         */
        boolean isRequireRestartService();
    }
}

