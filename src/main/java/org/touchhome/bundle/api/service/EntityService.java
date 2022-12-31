package org.touchhome.bundle.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.common.exception.NotFoundException;

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

    /**
     * Get service or throw error if not found
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
                    testService();
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return Optional.ofNullable((S) entityToService.get(getEntityID()));
        } finally {
            serviceAccessLock.unlock();
        }
    }

    default void testService() {
        try {
            if (getService().testService()) {
                setStatusOnline();
            }
        } catch (Exception ex) {
            setStatusError(ex);
        }
    }

    /**
     * Create service factory method
     *
     * @return service or null if service has to be created during some external process
     */
    @Nullable S createService(@NotNull EntityContext entityContext);

    @NotNull String getEntityID();

    default void destroyService() throws Exception {
        S service = (S) entityToService.remove(getEntityID());
        if (service != null) {
            service.destroy();
        }
    }

    interface ServiceInstance<E extends EntityService<?, ?>> {
        /**
         * Fires to update entity inside in-memory service each time when entity fetched/updated
         * testService() method calls always after this to check service actual status
         *
         * @return true - need call testService()
         */
        boolean entityUpdated(@NotNull E entity);

        @NotNull E getEntity();

        /**
         * Test service must check if service became/still available.
         *
         * @return true - set entity status to ONLINE. false - keep existed
         * @throws Exception - set entity status to ERROR
         */
        boolean testService() throws Exception;

        default void testServiceWithSetStatus() throws Exception {
            try {
                if (testService()) {
                    getEntity().setStatusOnline();
                }
            } catch (Exception ex) {
                getEntity().setStatusError(ex);
                throw ex;
            }
        }

        void destroy() throws Exception;
    }
}

