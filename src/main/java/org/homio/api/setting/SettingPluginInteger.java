package org.homio.api.setting;

import static org.homio.api.util.JsonUtils.putOpt;

import org.homio.api.Context;
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
    default @NotNull JSONObject getParameters(Context context, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(context, value);
        putOpt(parameters, "min", getMin());
        putOpt(parameters, "max", getMax());
        return parameters;
    }
}
