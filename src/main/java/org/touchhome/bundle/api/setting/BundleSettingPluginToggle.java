package org.touchhome.bundle.api.setting;

public interface BundleSettingPluginToggle extends BundleSettingPlugin<Boolean> {

    String getToggleIcon();

    @Override
    default SettingType getSettingType() {
        return SettingType.Toggle;
    }
}
