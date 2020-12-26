package org.touchhome.bundle.api.setting;

public interface SettingPluginBoolean extends SettingPlugin<Boolean> {

    default boolean defaultValue() {
        return false;
    }

    @Override
    default String getDefaultValue() {
        return Boolean.toString(defaultValue());
    }

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Boolean;
    }
}
