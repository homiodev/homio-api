package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

public interface BundleSettingPluginInteger extends BundleSettingPlugin<Integer> {

    @Override
    default SettingType getSettingType() {
        return SettingType.Integer;
    }

    default int getMin() {
        return Integer.MIN_VALUE;
    }

    default int getMax() {
        return Integer.MAX_VALUE;
    }

    int defaultValue();

    @Override
    default String getDefaultValue() {
        return String.valueOf(defaultValue());
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        return new JSONObject().put("min", getMin()).put("max", getMax());
    }
}
