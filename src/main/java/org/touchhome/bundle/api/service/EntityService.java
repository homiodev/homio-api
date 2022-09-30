package org.touchhome.bundle.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.entity.HasStatusAndMsg;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.common.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Configure service for entites. I.e. MongoEntity has MongoService which correspond for communications, RabbitMQ, etc...
 */
public interface EntityService<S, T extends HasEntityIdentifier> extends HasStatusAndMsg<T> {
    ReentrantLock serviceAccessLock = new ReentrantLock();

    Map<String, ServiceIdentifier> entityToService = new HashMap<>();

    Class<S> getEntityServiceItemClass();

    @SneakyThrows
    default S getOrCreateService(EntityContext entityContext, boolean throwIfError, boolean testService) {
        serviceAccessLock.lock();
        try {
            Object[] safeValues = Stream.of(getServiceParams()).map(v -> v == null ? "" : v).toArray();
            int hash = Objects.hash(safeValues);

            if (entityToService.containsKey(getEntityID()) && entityToService.get(getEntityID()).hashCode != hash) {
                destroyService();
            }
            ServiceIdentifier sid = entityToService.computeIfAbsent(getEntityID(), entityID -> {
                setStatus(Status.ONLINE, null);
                try {
                    return new ServiceIdentifier(hash, createService(entityContext));
                } catch (Exception ex) {
                    setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
                    if (throwIfError) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
            });
            if (sid != null) {
                try {
                    if (testService) {
                        setStatus(Status.ONLINE, null);
                        testService((S) sid.getService());
                    }
                } catch (Exception ex) {
                    setStatus(Status.ERROR, CommonUtils.getErrorMessage(ex));
                    if (throwIfError) {
                        throw new RuntimeException(ex);
                    }
                    return null;
                }
            }
            return sid == null ? null : (S) sid.service;
        } finally {
            serviceAccessLock.unlock();
        }
    }

    S createService(EntityContext entityContext) throws Exception;

    void testService(S service) throws Exception;

    @JsonIgnore
    Object[] getServiceParams();

    String getEntityID();

    default void destroyService() throws Exception {
        ServiceIdentifier serviceIdentifier = entityToService.remove(getEntityID());
        if (serviceIdentifier != null) {
            destroyService((S) serviceIdentifier.service);
        }
    }

    default void destroyService(S service) throws Exception {

    }

    @AllArgsConstructor
    class ServiceIdentifier {
        private int hashCode;
        @Getter
        private Object service;
    }
}

