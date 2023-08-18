package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import static org.homio.api.util.JsonUtils.putOpt;

public interface SettingPluginSlider extends SettingPluginInteger {

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Slider;
    }

    default Integer getStep() {
        return null;
    }

    default String getHeader() {
        return null;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPluginInteger.super.getParameters(entityContext, value);
        putOpt(parameters, "step", getStep());
        putOpt(parameters, "header", getHeader());
        return parameters;
    }
}
