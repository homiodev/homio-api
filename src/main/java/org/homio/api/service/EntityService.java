package org.homio.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.SneakyThrows;
import org.homio.api.EntityContext;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.HasEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        protected final EntityContext entityContext;
        protected final String entityID;
        protected E entity;
        protected long entityHashCode;

        public ServiceInstance(EntityContext entityContext, E entity) {
            this.entityContext = entityContext;
            this.entity = entity;
            this.entityID = entity.getEntityID();
            this.entityHashCode = getEntityHashCode(entity);
        }

        public void testServiceWithSetStatus() {
            try {
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
        public final void entityUpdated(@NotNull E newEntity) {
            long newEntityHashCode = getEntityHashCode(newEntity);
            boolean requireReinitialize = entityHashCode != newEntityHashCode;
            entityHashCode = newEntityHashCode;
            entity = newEntity;

            if (requireReinitialize) {
                fireInitialize();
            }
        }

        /**
         * @return watchdog if service supports watchdog capabilities
         */
        public @Nullable WatchdogService getWatchdog() {
            return null;
        }

        protected boolean fireInitialize() {
            try {
                initialize();
                return true;
            } catch (Exception ex) {
                entity.setStatusError(ex);
                return false;
            }
        }

        protected void testService() {

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

