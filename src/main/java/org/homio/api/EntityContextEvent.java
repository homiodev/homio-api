package org.homio.api;

import com.pivovarit.function.ThrowingRunnable;
import java.net.DatagramPacket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.homio.api.entity.BaseEntityIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextEvent {

    /**
     * Listen upd on host/port. default host is wildcard
     * listener accept DatagramPacket and string value
     * @param listener -
     * @param host -
     * @param key -
     * @param port -
     */
    void listenUdp(String key, @Nullable String host, int port, BiConsumer<DatagramPacket, String> listener);

    void stopListenUdp(String key);

    /**
     * Remove general listeners and last saved value
     *
     * @param key            - unique id
     * @param additionalKeys - additionalKeys
     */
    void removeEvents(String key, String... additionalKeys);

    /**
     * Listen for general event with key. Replace listener if key already exists
     * @param key - unique key
     * @param  listener - listener
     * @return this
     */
    default EntityContextEvent addEventListener(String key, Consumer<Object> listener) {
        return addEventListener(key, "", listener);
    }

    /**
     * Listen for general event with key. Replace listener if key already exists
     * @param key - unique key
     * @param discriminator - discriminator
     * @param listener - listener
     * @return EntityContextEvent
     */
    EntityContextEvent addEventListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     * @param key - unique key
     * @param listener - listener
     * @return EntityContextEvent
     */
    default EntityContextEvent addEventBehaviourListener(String key, Consumer<Object> listener) {
        return addEventBehaviourListener(key, "", listener);
    }

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     * @param key - unique key
     * @param discriminator - discriminator
     * @param listener - listener
     * @return EntityContextEvent
     */
    EntityContextEvent addEventBehaviourListener(String key, String discriminator, Consumer<Object> listener);

    /**
     * Fire event with key and value.
     *
     * @param value - must implement equal() method in case if compareValues is true
     * @param key - unique key
     * @return EntityContextEvent
     */
    EntityContextEvent fireEvent(@NotNull String key, @Nullable Object value);

    /**
     * Fire event with key and value only if previous saved value is null or value != previousValue
     * @param key - unique key
     * @param value - value to fire
     * @return EntityContextEvent
     */
    EntityContextEvent fireEventIfNotSame(@NotNull String key, @Nullable Object value);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
            (String entityID, String key, EntityUpdateListener<T> listener);

    /**t
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     * @param key - unique key
     * @param <T> -
     * @return this
     */
    <T extends BaseEntityIdentifier> EntityContextEvent addEntityUpdateListener
    (Class<T> entityClass, String key, Consumer<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     * @param key - key
     * @param <T> -
     * @return this
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
     * @param name - name
     * @param command - command
     */
    void runOnceOnInternetUp(@NotNull String name, @NotNull ThrowingRunnable<Exception> command);

    /**
     * Listen for any port changes with key. Replace listener if key already exists
     * @param key - key
     * @param listener - listener
     */
    default void addPortChangeStatusListener(String key, Consumer<Object> listener) {
        addEventListener("any-port-changed", key, listener);
    }

    interface EntityUpdateListener<T> {
        void entityUpdated(T newValue, T oldValue);
    }
}
