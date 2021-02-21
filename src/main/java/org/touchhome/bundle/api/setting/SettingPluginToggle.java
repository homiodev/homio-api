package org.touchhome.bundle.api.setting;

public interface SettingPluginToggle extends SettingPlugin<Boolean> {

    String getIcon();

    String getToggleIcon();

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Toggle;
    }
}
