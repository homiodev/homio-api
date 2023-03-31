package org.homio.bundle.api;

import com.pivovarit.function.ThrowingRunnable;
import java.util.function.Consumer;
import org.homio.bundle.api.entity.BaseEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextEvent {

    /**
     * Remove general listeners and last saved value
     */
    void removeEvents(String key, String... additionalKeys);

    /**
     * Listen for general event with key. Replace listener if key already exists
     */
    default EntityContextEvent addEventListener(String key, Consumer<Object> listener) {
        return addEventListener(key, "", listener);
    }

    /**
     * Listen for general event with key. Replace listener if key already exists
     */
    EntityContextEvent addEventListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     */
    default EntityContextEvent addEventBehaviourListener(String key, Consumer<Object> listener) {
        return addEventBehaviourListener(key, "", listener);
    }

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     */
    EntityContextEvent addEventBehaviourListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Fire event with key and value.
     *
     * @param value - must implement equal() method in case if compareValues is true
     */
    EntityContextEvent fireEvent(@NotNull String key, @Nullable Object value);

    /**
     * Fire event with key and value only if previous saved value is null or value != previousValue
     */
    EntityContextEvent fireEventIfNotSame(@NotNull String key, @Nullable Object value);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, EntityUpdateListener<T> listener);

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
    (Class<T> entityClass, String key, EntityUpdateListener<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityCreateListener
            (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityRemovedListener
            (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityRemovedListener
            (String entityID, String key, Consumer<T> listener);

    EntityContextEvent removeEntityUpdateListener(String entityID, String key);

    EntityContextEvent removeEntityRemoveListener(String entityID, String key);

    /**
     * Run only once when internet became available
     */
    void runOnceOnInternetUp(@NotNull String name, @NotNull ThrowingRunnable<Exception> command);

    /**
     * Listen for any port changes with key. Replace listener if key already exists
     */
    default void addPortChangeStatusListener(String key, Consumer<Object> listener) {
        addEventListener("any-port-changed", key, listener);
    }

    interface EntityUpdateListener<T> {
        void entityUpdated(T newValue, T oldValue);
    }
}
