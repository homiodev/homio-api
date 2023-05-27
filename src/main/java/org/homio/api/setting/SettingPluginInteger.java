package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.util.CommonUtils;
import org.json.JSONObject;

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
        CommonUtils.putOpt(parameters, "min", getMin());
        CommonUtils.putOpt(parameters, "max", getMax());
        return parameters;
    }
}
