package org.homio.api.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pivovarit.function.ThrowingRunnable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.SneakyThrows;
import org.homio.api.EntityContext;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.model.Status;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configure service for entities. I.e. MongoEntity has MongoService which correspond for communications, RabbitMQ, etc...
 */
public interface EntityService<S extends EntityService.ServiceInstance, T extends HasEntityIdentifier>
    extends HasStatusAndMsg {

    ReentrantLock serviceAccessLock = new ReentrantLock();

    EntityContext getEntityContext();

    @JsonIgnore
    long getEntityServiceHashCode();

    /**
     * @return Get service or throw error if not found
     */
    @JsonIgnore
    default @NotNull S getService() throws NotFoundException {
        Object service = getEntityContext().service().getEntityService(getEntityID());
        if (service == null) {
            throw new NotFoundException("Service for entity: " + getEntityID() + " not found");
        }
        return (S) service;
    }

    @JsonIgnore
    default @NotNull Optional<S> optService() {
        return Optional.ofNullable((S) getEntityContext().service().getEntityService(getEntityID()));
    }

    @JsonIgnore
    @NotNull Class<S> getEntityServiceItemClass();

    @SneakyThrows
    default @NotNull Optional<S> getOrCreateService(@NotNull EntityContext entityContext) {
        serviceAccessLock.lock();
        try {
            if (getEntityContext().service().isHasEntityService(getEntityID())) {
                return Optional.of((S) getEntityContext().service().getEntityService(getEntityID()));
            }
            try {
                S service = createService(entityContext);
                if (service != null) {
                    getEntityContext().service().addEntityService(getEntityID(), service);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            return Optional.ofNullable((S) getEntityContext().service().getEntityService(getEntityID()));
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
        S service = (S) getEntityContext().service().removeEntityService(getEntityID());
        if (service != null) {
            service.destroy();
        }
    }

    @Getter
    abstract class ServiceInstance<E extends EntityService<?, ?>> implements WatchdogService {

        protected final @NotNull EntityContext entityContext;
        protected final String entityID;
        private final AtomicBoolean initializing = new AtomicBoolean(false);
        protected E entity;
        protected long entityHashCode;

        public ServiceInstance(@NotNull EntityContext entityContext, @NotNull E entity, boolean fireFirstInitialize) {
            this.entityContext = entityContext;
            this.entityID = entity.getEntityID();
            this.entity = entity;
            this.entityHashCode = getEntityHashCode(entity);

            if (fireFirstInitialize) {
                entity.setStatus(Status.INITIALIZE);
                initializing.set(true);
                entityContext.bgp().execute(Duration.ofSeconds(1), () -> {
                    fireWithSetStatus(this::firstInitialize);
                    initializing.set(false);
                });
            }
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
        public void backupData(BackupContext backupContext) {

        }

        /**
         * Fires to update entity inside in-memory service each time when entity fetched/updated testService() method calls always after this to check service
         * actual status
         *
         * @param newEntity - updated entity
         */
        public void entityUpdated(@NotNull E newEntity) {
            long newEntityHashCode = getEntityHashCode(newEntity);
            boolean requireReinitialize = entityHashCode != newEntityHashCode;
            entityHashCode = newEntityHashCode;
            entity = newEntity;

            if (requireReinitialize) {
                entityContext.bgp().execute(() -> {
                    while (!initializing.compareAndSet(false, true)) {
                        Thread.yield();
                    }
                    entity.setStatus(Status.INITIALIZE);
                    fireWithSetStatus(this::initialize);
                    initializing.set(false);
                });
            }
        }

        /**
         * Async restarting service fired by watchdog service if isRequireRestartService return not null. Restart service in interval 1..2 minutes Service
         * should be as fast as possible. Use inner async if possible Method calls in ForkJoin pool at same time with other services if need
         */
        @Override
        public void restartService() {
            if (initializing.compareAndSet(false, true)) {
                fireWithSetStatus(this::initialize);
                initializing.set(false);
            }
        }

        /**
         * Executes +- every minute
         * @return Check if need restart service before call restartService().. Return restart reason or null if not require
         */
        @Override
        public String isRequireRestartService() {
            return null;
        }

        private void fireWithSetStatus(ThrowingRunnable<Exception> handler) {
            try {
                handler.run();
            } catch (Exception ex) {
                entity.setStatusError(ex);
            }
        }

        protected void testService() {

        }

        protected void firstInitialize() {
            // fallback to initialize
            initialize();

        }

        protected abstract void initialize();

        public abstract void destroy() throws Exception;

        protected long getEntityHashCode(E entity) {
            return entity.getEntityServiceHashCode();
        }

        public interface BackupContext {

            Path getBackupPath();
        }
    }

    interface WatchdogService {

        void restartService();

        String isRequireRestartService();
    }
}

