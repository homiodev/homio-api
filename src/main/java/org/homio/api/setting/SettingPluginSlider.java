package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.util.CommonUtils;
import org.json.JSONObject;

public interface SettingPluginSlider extends SettingPluginInteger {

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Slider;
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
        CommonUtils.putOpt(parameters, "step", getStep());
        CommonUtils.putOpt(parameters, "header", getHeader());
        return parameters;
    }
}