package org.touchhome.bundle.api;

import io.swagger.annotations.ApiParam;
import org.touchhome.bundle.api.setting.BundleSettingPlugin;

import javax.validation.constraints.NotNull;
import java.util.function.Consumer;

public interface SettingEntityContext {
    <T> T getSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    <T> String getSettingRawValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass);

    default <T> T getSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, T defaultValue) {
        T value = getSettingValue(settingClass);
        return value == null ? defaultValue : value;
    }

    default <T> void listenSettingValueAsync(@ApiParam("setting class") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener) {
        listenSettingValue(settingClass, key, value ->
                new Thread(() -> listener.accept(value), "run-listen-value-async-" + settingClass.getSimpleName()).start());
    }

    default <T> void listenSettingValueAsync(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenSettingValueAsync(settingClass, key, t -> listener.run());
    }

    default <T> void listenSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Runnable listener) {
        listenSettingValue(settingClass, key, p -> listener.run());
    }

    <T> void listenSettingValue(Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("unique key") String key, @ApiParam("listener") Consumer<T> listener);

    <T> void setSettingValueRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> bundleSettingPluginClazz, @ApiParam("value") @NotNull String value);

    <T> void setSettingValue(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    /**
     * Save setting value without firing events
     *
     * @return value converted to string
     */
    <T> String setSettingValueSilence(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull T value);

    <T> void setSettingValueSilenceRaw(@ApiParam("settingClass") Class<? extends BundleSettingPlugin<T>> settingClass, @ApiParam("value") @NotNull String value);
}
