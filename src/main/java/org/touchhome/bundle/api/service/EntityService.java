package org.touchhome.bundle.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.common.exception.NotFoundException;
import org.touchhome.common.util.CommonUtils;

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
     * Get service and throw error if not found
     */
    @JsonIgnore
    default S getService() throws NotFoundException {
        Object service = entityToService.get(getEntityID());
        if (service == null) {
            throw new NotFoundException("Service for entity: " + getEntityID() + " not found");
        }
        return (S) service;
    }

    @JsonIgnore
    default Optional<S> optService() {
        return Optional.ofNullable((S) entityToService.get(getEntityID()));
    }

    @JsonIgnore
    Class<S> getEntityServiceItemClass();

    @SneakyThrows
    default S getOrCreateService(EntityContext entityContext, boolean throwIfError, boolean testService) {
        serviceAccessLock.lock();
        try {
            S service = (S) entityToService.computeIfAbsent(getEntityID(), entityID -> {
                setStatus(getSuccessServiceStatus(), null);
                try {
                    return createService(entityContext);
                } catch (Exception ex) {
                    setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
                    if (throwIfError) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
            });
            if (service != null) {
                try {
                    if (testService) {
                        setStatus(getSuccessServiceStatus(), null);
                        service.testService();
                    }
                } catch (Exception ex) {
                    setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
                    if (throwIfError) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
            }
            return service;
        } finally {
            serviceAccessLock.unlock();
        }
    }

    S createService(EntityContext entityContext) throws Exception;

    /**
     * Mark service with getSuccessServiceStatus() status if service has been created
     * Do not mark status if method return null
     */
    default @Nullable Status getSuccessServiceStatus() {
        return Status.ONLINE;
    }

    String getEntityID();

    default void destroyService() throws Exception {
        S service = (S) entityToService.remove(getEntityID());
        if (service != null) {
            service.destroy();
        }
    }

    interface ServiceInstance<E extends EntityService<?, ?>> {
        /**
         * Fires to update entity inside in-memory service each time when entity fetched/updated
         */
        void entityUpdated(E entity);

        void destroy() throws Exception;

        void testService() throws Exception;
    }
}

