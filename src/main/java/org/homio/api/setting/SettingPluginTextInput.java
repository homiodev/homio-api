package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginTextInput extends SettingPlugin<String> {

    @Override
    default @NotNull Class<String> getType() {
        return String.class;
    }

    default String getPattern() {
        return null;
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.TextInput;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(entityContext, value);
        CommonUtils.putOpt(parameters, "pattern", getPattern());
        return parameters;
    }
}
