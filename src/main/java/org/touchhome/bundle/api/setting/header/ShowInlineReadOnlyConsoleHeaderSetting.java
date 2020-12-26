package org.touchhome.bundle.api.setting.header;

import org.touchhome.bundle.api.setting.SettingPluginToggle;

public class ShowInlineReadOnlyConsoleHeaderSetting implements HeaderSettingPlugin<Boolean>, SettingPluginToggle {

    @Override
    public String getIcon() {
        return "fas fa-file-code";
    }

    @Override
    public String getToggleIcon() {
        return "far fa-file-code";
    }

    @Override
    public boolean isStorable() {
        return false;
    }
}
