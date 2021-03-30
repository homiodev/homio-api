package org.touchhome.bundle.api.setting;

import org.touchhome.bundle.api.ui.field.UIFieldType;

public interface SettingPluginInfo extends SettingPlugin<String> {

    @Override
    default Class<String> getType() {
        return String.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Info;
    }

    @Override
    default boolean isStorable() {
        return false;
    }
}
