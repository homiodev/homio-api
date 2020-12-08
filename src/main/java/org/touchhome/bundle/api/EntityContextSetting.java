package org.touchhome.bundle.api;

import org.touchhome.bundle.api.setting.BundleSettingOptionsSettingPlugin;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;
import org.touchhome.bundle.api.setting.header.dynamic.BundleDynamicHeaderSettingPlugin;
import org.touchhome.bundle.api.setting.header.dynamic.BundleHeaderDynamicContainerSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;

public interface EntityContextSetting {

    /**
     * Update setting components on ui
     */
    void reloadSettings(Class<? extends BundleSettingOptionsSettingPlugin> settingPlugin);

    /**
     * Update setting components on ui. Uses for updating dynamic settings
     */
    void reloadSettings(Class<? extends BundleHeaderDynamicContainerSettingPlugin> dynamicSettingPluginClass,
                        List<? extends BundleDynamicHeaderSettingPlugin> dynamicSettings);

    <T> T getValue(Class<? extends BundleSettingPlugin<T>> settingClass);

    <T> String getRawValue(Class<? extends BundleSettingPlugin<T>> settingClass);

    default <T> T getValue(Class<? extends BundleSettingPlugin<T>> settingClass, T defaultValue) {
        T value = getValue(settingClass);
        return value == null ? defaultValue : value;
    }

    <T> void listenValueAsync(Class<? extends BundleSettingPlugin<T>> settingClass, String key, Consumer<T> listener);

    default <T> void listenValueAsync(Class<? extends BundleSettingPlugin<T>> settingClass, String key, Runnable listener) {
        listenValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenValue(Class<? extends BundleSettingPlugin<T>> settingClass, String key, Runnable listener) {
        listenValue(settingClass, key, p -> listener.run());
    }

    <T> void listenValue(Class<? extends BundleSettingPlugin<T>> settingClass, String key, Consumer<T> listener);

    default <T> void listenValueAndGet(Class<? extends BundleSettingPlugin<T>> settingClass, String key, Consumer<T> listener) {
        listenValue(settingClass, key, listener);
        listener.accept(getValue(settingClass));
    }

    <T> void setValueRaw(Class<? extends BundleSettingPlugin<T>> bundleSettingPluginClazz, @NotNull String value);

    <T> void setValue(Class<? extends BundleSettingPlugin<T>> settingClass, @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setValueSilence(Class<? extends BundleSettingPlugin<T>> settingClass, @NotNull T value);

    <T> void setValueSilenceRaw(Class<? extends BundleSettingPlugin<T>> settingClass, @NotNull String value);
}
