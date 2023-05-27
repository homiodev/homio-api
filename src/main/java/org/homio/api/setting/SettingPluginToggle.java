package org.homio.api.setting;

import org.homio.api.ui.field.UIFieldType;

public interface SettingPluginToggle extends SettingPlugin<Boolean> {

    String getIcon();

    String getToggleIcon();

    default String getIconColor() {
        return "";
    }

    default String getToggleIconColor() {
        return "";
    }

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Toggle;
    }
}
