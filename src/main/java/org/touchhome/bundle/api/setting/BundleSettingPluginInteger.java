package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import static org.touchhome.bundle.api.util.TouchHomeUtils.putOpt;

public interface BundleSettingPluginInteger extends BundleSettingPlugin<Integer> {

    @Override
    default SettingType getSettingType() {
        return SettingType.Integer;
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
        JSONObject parameters = BundleSettingPlugin.super.getParameters(entityContext, value);
        putOpt(parameters, "min", getMin());
        putOpt(parameters, "max", getMax());
        return parameters;
    }
}
