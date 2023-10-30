package org.homio.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.exception.NotFoundException;
import org.homio.api.storage.DataStorageEntity;
import org.homio.api.storage.DataStorageService;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextStorage {

    Map<Class<? extends BaseEntity>, BaseEntity> ENTITY_CLASS_TO_POJO = new HashMap<>();

    @NotNull Context context();

    default @Nullable <T extends BaseEntity> T getEntity(@NotNull String entityID) {
        return getEntity(entityID, true);
    }

    default @Nullable <T extends BaseEntity> T getEntity(Class<T> entityClass, @NotNull String entityID) {
        BaseEntity entity = ENTITY_CLASS_TO_POJO.computeIfAbsent(entityClass, CommonUtils::newInstance);
        return getEntity(entity.setEntityID(entityID), true);
    }

    default @NotNull <T extends BaseEntity> T getEntityRequire(@NotNull String entityID) {
        T entity = getEntity(entityID, true);
        if (entity == null) {
            throw new NotFoundException("Unable to find entity: " + entityID);
        }
        return entity;
    }

    default @NotNull <T extends BaseEntity> T getEntityRequire(Class<T> entityClass, @NotNull String entityID) {
        T entity = CommonUtils.newInstance(entityClass);
        return getEntityRequire(entity.setEntityID(entityID));
    }

    default @Nullable <T extends BaseEntity> T getEntityOrDefault(@NotNull String entityID, @Nullable T defEntity) {
        T entity = getEntity(entityID, true);
        return entity == null ? defEntity : entity;
    }

    @Nullable BaseEntity delete(@NotNull String entityId);

    /**
     * Get entity by entityID.
     *
     * @param entityID - entity unique id to fetch
     * @param useCache - allow to use cache or direct db
     * @param <T>      -
     * @return base entity
     */
    @Nullable <T extends BaseEntity> T getEntity(@NotNull String entityID, boolean useCache);

    default @Nullable <T extends BaseEntity> T getEntity(@NotNull T entity) {
        return getEntity(entity.getEntityID());
    }

    <T extends BaseEntity> void createDelayed(@NotNull T entity);

    <T extends BaseEntity> void updateDelayed(@NotNull T entity, @NotNull Consumer<T> fieldUpdateConsumer);

    default @NotNull <T extends BaseEntity> T save(@NotNull T entity) {
        return save(entity, true);
    }

    @NotNull <T extends BaseEntity> T save(@NotNull T entity, boolean fireNotifyListeners);

    default @Nullable <T extends BaseEntity> T delete(@NotNull T entity) {
        return (T) delete(entity.getEntityID());
    }

    default @Nullable <T extends BaseEntity> T findAny(@NotNull Class<T> clazz) {
        List<T> list = findAll(clazz);
        return list.isEmpty() ? null : list.iterator().next();
    }

    @NotNull <T extends BaseEntity> List<T> findAll(@NotNull Class<T> clazz);

    @NotNull <T extends BaseEntity> List<T> findAllByPrefix(@NotNull String prefix);

    default @NotNull <T extends BaseEntity> List<T> findAll(@NotNull T entity) {
        return (List<T>) findAll(entity.getClass());
    }

    default <T extends DataStorageEntity> DataStorageService<T> getOrCreateInMemoryService(
        @NotNull Class<T> pojoClass, @Nullable Long quota) {
        return getOrCreateInMemoryService(pojoClass, pojoClass.getSimpleName(), quota);
    }

    <T extends DataStorageEntity> DataStorageService<T> getOrCreateInMemoryService(
        @NotNull Class<T> pojoClass, @NotNull String uniqueId, @Nullable Long quota);

    List<DeviceBaseEntity> getDeviceEntity(@NotNull String ieeeAddress, @Nullable String typePrefix);
}
