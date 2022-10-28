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
    default EntityContextEvent addEventListener(String key, Consumer<Object> listener) {
        return addEventListener(key, "", listener);
    }

    EntityContextEvent addEventListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     */
    default EntityContextEvent addEventBehaviourListener(String key, Consumer<Object> listener) {
        return addEventBehaviourListener(key, "", listener);
    }

    EntityContextEvent addEventBehaviourListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Fire event with key and value
     *
     * @param value - must implement equal() method in case if compareValues is true
     */
    EntityContextEvent fireEvent(@NotNull String key, @Nullable Object value, boolean compareValues);

    default EntityContextEvent fireEvent(@NotNull String key, @Nullable Object value) {
        return fireEvent(key, value, true);
    }

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, EntityContext.EntityUpdateListener<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     */
    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
    (Class<T> entityClass, String key, Consumer<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     */
    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
    (Class<T> entityClass, String key, EntityContext.EntityUpdateListener<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityCreateListener
            (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityRemovedListener
            (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityRemovedListener
            (String entityID, String key, Consumer<T> listener);

    EntityContextEvent removeEntityUpdateListener(String entityID, String key);

    EntityContextEvent removeEntityRemoveListener(String entityID, String key);
}
