package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

public interface BundleSettingPluginSlider extends BundleSettingPluginInteger {

    @Override
    default SettingType getSettingType() {
        return SettingType.Slider;
    }

    default int getStep() {
        return 1;
    }

    default String getHeader() {
        return null;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        return BundleSettingPluginInteger.super.getParameters(entityContext, value)
                .put("step", getStep()).put("header", getHeader());
    }
}
