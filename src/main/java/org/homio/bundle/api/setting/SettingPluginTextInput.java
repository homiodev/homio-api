package org.homio.bundle.api.setting;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.util.TouchHomeUtils;
import org.json.JSONObject;

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
        TouchHomeUtils.putOpt(parameters, "pattern", getPattern());
        return parameters;
    }
}
