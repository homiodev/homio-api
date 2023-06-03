package org.homio.api.setting;

import org.homio.api.model.Icon;
import org.homio.api.ui.field.UIFieldType;

public interface SettingPluginToggle extends SettingPlugin<Boolean> {

    Icon getIcon();

    Icon getToggleIcon();

    @Override
    default Class<Boolean> getType() {
        return Boolean.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Toggle;
    }
}
