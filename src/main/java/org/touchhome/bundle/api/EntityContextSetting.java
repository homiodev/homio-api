package org.touchhome.bundle.api;

import org.touchhome.bundle.api.setting.SettingPlugin;
import org.touchhome.bundle.api.setting.SettingPluginOptions;
import org.touchhome.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderContainerSettingPlugin;
import org.touchhome.bundle.api.setting.console.header.dynamic.DynamicConsoleHeaderSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;

public interface EntityContextSetting {

    /**
     * Update setting components on ui
     */
    void reloadSettings(Class<? extends SettingPluginOptions> settingPlugin);

    /**
     * Update setting components on ui. Uses for updating dynamic settings
     */
    void reloadSettings(Class<? extends DynamicConsoleHeaderContainerSettingPlugin> dynamicSettingPluginClass,
                        List<? extends DynamicConsoleHeaderSettingPlugin> dynamicSettings);

    /**
     * Get setting value by class name
     */
    <T> T getValue(Class<? extends SettingPlugin<T>> settingClass);

    /**
     * Get unparsed setting value by class name
     */
    <T> String getRawValue(Class<? extends SettingPlugin<T>> settingClass);

    /**
     * Get setting value by class name or default value if null
     */
    default <T> T getValue(Class<? extends SettingPlugin<T>> settingClass, T defaultValue) {
        T value = getValue(settingClass);
        return value == null ? defaultValue : value;
    }

    /**
     * Subscribe for setting changes. Key requires to able to unsubscribe
     */
    <T> void listenValueAsync(Class<? extends SettingPlugin<T>> settingClass, String key, Consumer<T> listener);

    /**
     * Subscribe for setting changes. listener fires in separate thread
     */
    default <T> void listenValueAsync(Class<? extends SettingPlugin<T>> settingClass, String key, Runnable listener) {
        listenValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenValue(Class<? extends SettingPlugin<T>> settingClass, String key, Runnable listener) {
        listenValue(settingClass, key, p -> listener.run());
    }

    <T> void listenValue(Class<? extends SettingPlugin<T>> settingClass, String key, Consumer<T> listener);

    <T> void unListenValue(Class<? extends SettingPlugin<T>> settingClass, String key);

    default <T> void listenValueAndGet(Class<? extends SettingPlugin<T>> settingClass, String key, Consumer<T> listener) {
        listenValue(settingClass, key, listener);
        listener.accept(getValue(settingClass));
    }

    <T> void setValueRaw(Class<? extends SettingPlugin<T>> bundleSettingPluginClazz, @NotNull String value);

    <T> void setValue(Class<? extends SettingPlugin<T>> settingClass, @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setValueSilence(Class<? extends SettingPlugin<T>> settingClass, @NotNull T value);

    <T> void setValueSilenceRaw(Class<? extends SettingPlugin<T>> settingClass, @NotNull String value);
}
