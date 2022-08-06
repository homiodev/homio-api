package org.touchhome.bundle.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.BaseEntityIdentifier;

import java.util.function.Consumer;

public interface EntityContextEvent {

    /**
     * Remove listeners and last saved value
     */
    void removeEvents(String key, String... additionalKeys);

    /**
     * Listen for event with key. Replace listener if key already exists
     */
    default void addEventListener(String key, Consumer<Object> listener) {
        addEventListener(key, "", listener);
    }

    void addEventListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     */
    default void addEventBehaviourListener(String key, Consumer<Object> listener) {
        addEventBehaviourListener(key, "", listener);
    }

    void addEventBehaviourListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Fire event with key and value
     *
     * @param value - must implement equal() method in case if compareValues is true
     */
    void fireEvent(@NotNull String key, @Nullable Object value, boolean compareValues);

    default void fireEvent(@NotNull String key, @Nullable Object value) {
        fireEvent(key, value, true);
    }

    <T extends BaseEntityIdentifier> void addEntityUpdateListener(String entityID, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> void addEntityUpdateListener(String entityID, String key,
                                                                  EntityContext.EntityUpdateListener<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     */
    <T extends BaseEntityIdentifier> void addEntityUpdateListener(Class<T> entityClass, String key, Consumer<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     */
    <T extends BaseEntityIdentifier> void addEntityUpdateListener(Class<T> entityClass, String key,
                                                                  EntityContext.EntityUpdateListener<T> listener);

    <T extends BaseEntityIdentifier> void addEntityCreateListener(Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> void addEntityRemovedListener(Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> void addEntityRemovedListener(String entityID, String key, Consumer<T> listener);

    void removeEntityUpdateListener(String entityID, String key);

    void removeEntityRemoveListener(String entityID, String key);
}
