package org.homio.api;

import org.homio.api.storage.DataStorageEntity;
import org.homio.api.storage.DataStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextStorage {

    @NotNull EntityContext getEntityContext();

    default <T extends DataStorageEntity> DataStorageService<T> getOrCreateInMemoryService(
            @NotNull Class<T> pojoClass, @Nullable Long quota) {
        return getOrCreateInMemoryService(pojoClass, pojoClass.getSimpleName(), quota);
    }

    <T extends DataStorageEntity> DataStorageService<T> getOrCreateInMemoryService(
            @NotNull Class<T> pojoClass, @NotNull String uniqueId, @Nullable Long quota);
}
