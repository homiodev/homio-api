package org.homio.api;

import com.pivovarit.function.ThrowingRunnable;
import java.net.DatagramPacket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextEvent {

    /**
     * Listen upd on host/port. default host is wildcard listener accept DatagramPacket and string value
     *
     * @param listener -
     * @param host     -
     * @param key      -
     * @param port     -
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

    void removeEventListener(String discriminator, String key);

    /**
     * Listen for general event with key. Replace listener if key already exists
     *
     * @param key      - unique key
     * @param listener - listener
     * @return this
     */
    default ContextEvent addEventListener(String key, Consumer<State> listener) {
        return addEventListener(key, "", listener);
    }

    /**
     * Listen for general event with key. Replace listener if key already exists
     *
     * @param key           - unique key
     * @param discriminator - discriminator
     * @param listener      - listener
     * @return ContextEvent
     */
    ContextEvent addEventListener(String key, String discriminator, Consumer<State> listener);

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     *
     * @param key      - unique key
     * @param listener - listener
     * @return ContextEvent
     */
    default ContextEvent addEventBehaviourListener(String key, Consumer<State> listener) {
        return addEventBehaviourListener(key, "", listener);
    }

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     *
     * @param key           - unique key
     * @param discriminator - discriminator
     * @param listener      - listener
     * @return ContextEvent
     */
    ContextEvent addEventBehaviourListener(String key, String discriminator, Consumer<State> listener);

    /**
     * Fire event with key and value.
     *
     * @param value - must implement equal() method in case if compareValues is true
     * @param key   - unique key
     * @return ContextEvent
     */
    ContextEvent fireEvent(@NotNull String key, @Nullable State value);

    /**
     * Fire event with key and value only if previous saved value is null or value != previousValue
     *
     * @param key   - unique key
     * @param value - value to fire
     * @return ContextEvent
     */
    ContextEvent fireEventIfNotSame(@NotNull String key, @Nullable State value);

    // go through all discriminators and count if key exists
    int getEventCount(@NotNull String key);

    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
        (String entityID, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
        (String entityID, String key, EntityUpdateListener<T> listener);

    /**
     * t Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     * @param key         - unique key
     * @param <T>         -
     * @return this
     */
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
    (Class<T> entityClass, String key, Consumer<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     * @param key         - key
     * @param <T>         -
     * @return this
     */
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
    (Class<T> entityClass, String key, EntityUpdateListener<T> listener);

    <T extends BaseEntityIdentifier> ContextEvent addEntityCreateListener
        (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> ContextEvent addEntityRemovedListener
        (Class<T> entityClass, String key, Consumer<T> listener);

    <T extends BaseEntityIdentifier> ContextEvent addEntityRemovedListener
        (String entityID, String key, Consumer<T> listener);

    ContextEvent removeEntityUpdateListener(String entityID, String key);

    ContextEvent removeEntityRemoveListener(String entityID, String key);

    /**
     * Run only once when internet became available. command executes in separate thread
     *
     * @param name    - name
     * @param command - command
     */
    void runOnceOnInternetUp(@NotNull String name, @NotNull ThrowingRunnable<Exception> command);

    boolean isInternetUp();

    default void ensureInternetUp(String message) {
        if (!isInternetUp()) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Listen for any port changes with key. Replace listener if key already exists
     *
     * @param key      - key
     * @param listener - listener
     */
    default void addPortChangeStatusListener(String key, Consumer<State> listener) {
        addEventListener("any-port-changed", key, listener);
    }

    interface EntityUpdateListener<T> {

        void entityUpdated(T newValue, T oldValue);
    }
}
