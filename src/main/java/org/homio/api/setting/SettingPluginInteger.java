package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginInteger extends SettingPlugin<Integer> {

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Integer;
    }

    default Integer getMin() {
        return null;
    }

    default Integer getMax() {
        return null;
    }

    @Override
    default @NotNull Class<Integer> getType() {
        return Integer.class;
    }

    int defaultValue();

    @Override
    default @NotNull String getDefaultValue() {
        return String.valueOf(defaultValue());
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(entityContext, value);
        CommonUtils.putOpt(parameters, "min", getMin());
        CommonUtils.putOpt(parameters, "max", getMax());
        return parameters;
    }
}
