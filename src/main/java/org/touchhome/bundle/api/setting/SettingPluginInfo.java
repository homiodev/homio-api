package org.touchhome.bundle.api.setting;

public interface SettingPluginInfo extends SettingPlugin<String> {

    @Override
    default Class<String> getType() {
        return String.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Info;
    }

    @Override
    default boolean isStorable() {
        return false;
    }
}
