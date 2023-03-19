package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import static org.touchhome.bundle.api.util.TouchHomeUtils.putOpt;

public interface SettingPluginTextInput extends SettingPlugin<String> {
    @Override
    default Class<String> getType() {
        return String.class;
    }

    default String getPattern() {
        return null;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.TextInput;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(entityContext, value);
        putOpt(parameters, "pattern", getPattern());
        return parameters;
    }
}
