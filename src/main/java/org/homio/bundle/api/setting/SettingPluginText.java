package org.homio.bundle.api.setting;

import org.homio.bundle.api.ui.field.UIFieldType;

public interface SettingPluginText extends SettingPlugin<String> {
    @Override
    default Class<String> getType() {
        return String.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Text;
    }
}
