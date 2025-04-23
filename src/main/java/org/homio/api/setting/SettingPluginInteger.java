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

    int getMin();

    int getMax();

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
        parameters.put("min", getMin());
        parameters.put("max", getMax());
        putOpt(parameters, "requireApply", isHasApplyButton());
        return parameters;
    }

    default Boolean isHasApplyButton() {
        return true;
    }
}
