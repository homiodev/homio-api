package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.ui.field.UIFieldType;

import static org.touchhome.bundle.api.util.TouchHomeUtils.putOpt;

public interface SettingPluginInteger extends SettingPlugin<Integer> {

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Integer;
    }

    default Integer getMin() {
        return null;
    }

    default Integer getMax() {
        return null;
    }

    @Override
    default Class<Integer> getType() {
        return Integer.class;
    }

    int defaultValue();

    @Override
    default String getDefaultValue() {
        return String.valueOf(defaultValue());
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(entityContext, value);
        putOpt(parameters, "min", getMin());
        putOpt(parameters, "max", getMax());
        return parameters;
    }
}
