package org.homio.api;

import com.pivovarit.function.ThrowingRunnable;
import java.time.Duration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.homio.api.entity.BaseEntityIdentifier;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextEvent {

    /**
     * Remove general listeners and last saved value
     *
     * @param key            - unique id
     * @param additionalKeys - additionalKeys
     */
    void removeEvents(@NotNull String key, String... additionalKeys);

    void removeEventListener(@Nullable String discriminator, @NotNull String key);

    /**
     * Listen for general event with key. Replace listener if key already exists
     *
     * @param key      - unique key
     * @param listener - listener
     * @return this
     */
    @NotNull
    default ContextEvent addEventListener(@NotNull String key, @NotNull Consumer<State> listener) {
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
    @NotNull
    ContextEvent addEventListener(@NotNull String key, @Nullable String discriminator, @Nullable Duration ttl, @NotNull Consumer<State> listener);

    @NotNull
    default ContextEvent addEventListener(@NotNull String key, @Nullable String discriminator, @NotNull Consumer<State> listener) {
        return addEventListener(key, discriminator, null, listener);
    }

    /**
     * Listen for event with key. Fires listener immediately if value was saved before
     *
     * @param key      - unique key
     * @param listener - listener
     * @return ContextEvent
     */
    @NotNull
    default ContextEvent addEventBehaviourListener(@NotNull String key, @NotNull Consumer<State> listener) {
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
    @NotNull
    ContextEvent addEventBehaviourListener(@NotNull String key, @Nullable String discriminator, @Nullable Duration ttl,
                                           @NotNull Consumer<State> listener);

    default @NotNull ContextEvent addEventBehaviourListener(@NotNull String key, @Nullable String discriminator, @NotNull Consumer<State> listener) {
        return addEventBehaviourListener(key, discriminator, null, listener);
    }

    @NotNull
    ContextEvent addEventBehaviourListener(@NotNull Pattern regexp, @Nullable String discriminator, @NotNull BiConsumer<String, State> listener);

    /**
     * Fire event with key and value.
     *
     * @param value - must implement equal() method in case if compareValues is true
     * @param key   - unique key
     * @return ContextEvent
     */
    @NotNull
    ContextEvent fireEvent(@NotNull String key, @Nullable State value);

    @NotNull
    default ContextEvent fireDeviceStatus(@NotNull String key, @NotNull HasStatusAndMsg entity) {
        return fireEvent(key + "-status-" + entity.getEntityID(), new StringType(entity.getStatus().name()));
    }

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

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityStatusUpdateListener
            (@NotNull String entityID, @NotNull String key, @NotNull Consumer<T> listener);

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
            (@NotNull String entityID, @NotNull String key, @NotNull Consumer<T> listener);

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
            (@NotNull String entityID, @NotNull String key, @NotNull EntityUpdateListener<T> listener);

    /**
     * t Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update
     * @param key         - unique key
     * @param <T>         -
     * @return this
     */
    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
    (@NotNull Class<T> entityClass, @NotNull String key, @NotNull Consumer<T> listener);

    /**
     * Listen any changes fot BaseEntity of concrete type.
     *
     * @param entityClass type to listen
     * @param listener    handler invoke when entity update. OldValue/NewValue
     * @param key         - key
     * @param <T>         -
     * @return this
     */
    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityUpdateListener
    (@NotNull Class<T> entityClass, @NotNull String key, @NotNull EntityUpdateListener<T> listener);

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityCreateListener
            (@NotNull Class<T> entityClass, @NotNull String key, @NotNull Consumer<T> listener);

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityRemovedListener
            (@NotNull Class<T> entityClass, @NotNull String key, @NotNull Consumer<T> listener);

    @NotNull
    <T extends BaseEntityIdentifier> ContextEvent addEntityRemovedListener
            (@NotNull String entityID, @NotNull String key, @NotNull Consumer<T> listener);

    @NotNull
    ContextEvent removeEntityUpdateListener(@NotNull String entityID, @NotNull String key);

    @NotNull
    ContextEvent removeEntityRemoveListener(@NotNull String entityID, @NotNull String key);

    /**
     * Run only once when internet became available. command executes in separate thread
     *
     * @param name    - name
     * @param command - command
     */
    void runOnceOnInternetUp(@NotNull String name, @NotNull ThrowingRunnable<Exception> command);

    boolean isInternetUp();

    default void onInternetStatusChanged(@NotNull String key, @NotNull Consumer<Boolean> listener) {
        addEventListener("internet-status", key, state -> listener.accept(state.boolValue()));
    }

    default void ensureInternetUp(@NotNull String message) {
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
    default void addPortChangeStatusListener(@NotNull String key, @NotNull Consumer<State> listener) {
        addEventListener("any-port-changed", key, listener);
    }

    interface EntityUpdateListener<T> {

        void entityUpdated(@NotNull T newValue, @Nullable T oldValue);
    }
}
