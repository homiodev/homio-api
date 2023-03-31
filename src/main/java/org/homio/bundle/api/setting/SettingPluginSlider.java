package org.homio.bundle.api.setting;

import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.util.TouchHomeUtils;
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
        TouchHomeUtils.putOpt(parameters, "step", getStep());
        TouchHomeUtils.putOpt(parameters, "header", getHeader());
        return parameters;
    }
}
