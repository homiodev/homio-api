package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.ui.field.UIFieldType;

public interface SettingPluginToggle extends SettingPlugin<Boolean> {

    String getIcon();

    String getToggleIcon();

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Toggle;
    }
}
