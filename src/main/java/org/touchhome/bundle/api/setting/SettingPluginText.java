package org.touchhome.bundle.api.setting;

public interface SettingPluginText extends SettingPlugin<String> {
    @Override
    default Class<String> getType() {
        return String.class;
    }

    @Override
    default SettingType getSettingType() {
        return SettingType.Text;
    }
}
