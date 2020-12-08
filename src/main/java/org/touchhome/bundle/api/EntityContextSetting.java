package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public interface EntityContextSetting {
    <T> T getValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    <T> String getRawValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    default <T> T getValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, T defaultValue) {
        T value = getValue(settingClass);
        return value == null ? defaultValue : value;
    }

    default <T> void listenValueAsync(@ApiParam("setting class") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener) {
        listenValue(settingClass, key, value ->
                new Thread(() -> listener.accept(value), "run-listen-value-async-" + settingClass.getSimpleName()).start());
    }

    default <T> void listenValueAsync(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenValue(settingClass, key, p -> listener.run());
    }

    <T> void listenValue(Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener);

    default <T> void listenValueAndGet(Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener) {
        listenValue(settingClass, key, listener);
        listener.accept(getValue(settingClass));
    }

    <T> void setValueRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> bundleSettingPluginClazz, @ApiParam("value") @NotNull String value);

    <T> void setValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setValueSilence(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    <T> void setValueSilenceRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull String value);
}
