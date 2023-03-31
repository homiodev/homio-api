package org.homio.bundle.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingRunnable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.SystemUtils;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.model.Status;
import org.homio.bundle.api.setting.SettingPlugin;
import org.homio.bundle.api.setting.SettingPluginOptions;
import org.homio.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderContainerSettingPlugin;
import org.homio.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        setMemValue(entity, distinguishKey + "Message", "", message);
        setMemValue(entity, distinguishKey, title, status);
    }

    static String getMessage(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey) {
        return getMemValue(entity, distinguishKey + "Message", null);
    }

    static void setMessage(@NotNull HasEntityIdentifier entity, @NotNull String distinguishKey, String message) {
        setMemValue(entity, distinguishKey + "Message", "", message);
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

    default <T> void listenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                 @NotNull ThrowingRunnable<Exception> listener) {
        listenValue(settingClass, key, p -> listener.run());
    }

    /**
     * Usually listeners executes in separate thread but in some cases we need access to user who updating value and we need
     * run listener inside http reques
     */
    <T> void listenValueInRequest(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                  @NotNull ThrowingConsumer<T, Exception> listener);

    <T> void listenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                         @NotNull ThrowingConsumer<T, Exception> listener);

    <T> void unListenValue(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key);

    default <T> void listenValueAndGet(@NotNull Class<? extends SettingPlugin<T>> settingClass, @NotNull String key,
                                       @NotNull ThrowingConsumer<T, Exception> listener) throws Exception {
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
