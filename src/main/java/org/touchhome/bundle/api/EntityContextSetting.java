package org.touchhome.bundle.api;

import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.model.Status;
import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.api.setting.SettingPluginOptions;
import org.touchhome.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderContainerSettingPlugin;
import org.touchhome.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public interface EntityContextSetting {

    AtomicReference<MemSetterHandler> MEM_HANDLER = new AtomicReference<>();

    static boolean isDevEnvironment() {
        return "true".equals(System.getProperty("development"));
    }

    static boolean isDockerEnvironment() {
        return "true".equals(System.getProperty("docker"));
    }

    static boolean isLinuxEnvironment() {
        return SystemUtils.IS_OS_LINUX && !isDockerEnvironment() && !isDevEnvironment();
    }

    static boolean isLinuxOrDockerEnvironment() {
        return SystemUtils.IS_OS_LINUX && !isDevEnvironment();
    }

    static <T> T getMemValue(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, @Nullable T defaultValue) {
        return (T) MEM_HANDLER.get().getValue(entity, distinguishKey, defaultValue);
    }

    /**
     * Set some value in memory and associate it with entity
     *
     * @param entity         - entity to associate with
     * @param distinguishKey - unqiue key for stored value that associanted with entity
     * @param title          - uses to log updated value to console
     * @param value          - value to store. remove from map if null
     */
    static <T> void setMemValue(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, @NotNull String title,
                                @Nullable T value) {
        MEM_HANDLER.get().setValue(entity, distinguishKey, title, value);
    }

    static Status getStatus(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, Status defaultStatus) {
        return getMemValue(entity, distinguishKey, defaultStatus);
    }

    static void setStatus(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, String title, Status status) {
        setMemValue(entity, distinguishKey, title, status);
    }

    static void setStatus(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, String title, Status status,
                          String message) {
        setMemValue(entity, distinguishKey + "_msg", "", message);
        setMemValue(entity, distinguishKey, title, status);
    }

    static String getMessage(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey) {
        return getMemValue(entity, distinguishKey + "_msg", null);
    }

    static void setMessage(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, String message) {
        setMemValue(entity, distinguishKey + "_msg", "", message);
    }

    /**
     * Get unmodifiable list of all available places. Configured via settings
     */
    List<String> getPlaces();

    /**
     * Update setting components on ui
     */
    void reloadSettings(@NotNull Class<? extends SettingPluginOptions> settingPlugin);

    /**
     * Update setting components on ui. Uses for updating dynamic settings
     */
    void reloadSettings(@NotNull Class<? extends DynamicConsoleHeaderContainerSettingPlugin> dynamicSettingPluginClass,
                        @NotNull List<? extends DynamicConsoleHeaderSettingPlugin> dynamicSettings);

    /**
     * Get setting value by class name
     */
    <T> T getValue(@NotNull Class<? extends SettingPlugin<T>> settingClass);

    /**
     * Get unparsed setting value by class name
     */
    <T> String getRawValue(@NotNull Class<? extends SettingPlugin<T>> settingClass);

    /**
     * Get setting value by class name or default value if null
     */
    default <T> T getValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @Nullable T defaultValue) {
        T value = getValue(settingClass);
        return value == null ? defaultValue : value;
    }

    /**
     * Subscribe for setting changes. Key requires to able to unsubscribe
     */
    <T> void listenValueAsync(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                              @NotNull Consumer<T> listener);

    /**
     * Subscribe for setting changes. listener fires in separate thread
     */
    default <T> void listenValueAsync(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                      @NotNull Runnable listener) {
        listenValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                 @NotNull Runnable listener) {
        listenValue(settingClass, key, p -> listener.run());
    }

    <T> void listenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                         @NotNull Consumer<T> listener);

    <T> void unListenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key);

    default <T> void listenValueAndGet(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                       @NotNull Consumer<T> listener) {
        listenValue(settingClass, key, listener);
        listener.accept(getValue(settingClass));
    }

    <T> void setValueRaw(@NotNull Class<? extends SettingPlugin<T>> bundleSettingPluginClazz, @NotNull String value);

    <T> void setValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setValueSilence(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull T value);

    <T> void setValueSilenceRaw(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String value);

    interface MemSetterHandler {
        void setValue(@NotNull HasEntityIdentifier entity, @NotNull String key, @NotNull String title, @Nullable Object value);

        Object getValue(@NotNull HasEntityIdentifier entity, @NotNull String key, @Nullable Object defaultValue);
    }
}
