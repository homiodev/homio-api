package org.homio.api.setting;

import org.homio.api.ui.field.UIFieldType;

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
    default UIFieldType getSettingType() {
        return UIFieldType.Boolean;
    }
}
