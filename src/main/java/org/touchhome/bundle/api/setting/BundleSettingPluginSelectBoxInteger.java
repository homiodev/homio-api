package org.touchhome.bundle.api.setting;

import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.Option;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface BundleSettingPluginSelectBoxInteger extends BundleSettingPluginInteger {
    @Override
    default SettingType getSettingType() {
        return SettingType.SelectBox;
    }

    @Override
    default Collection<Option> loadAvailableValues(EntityContext entityContext) {
        List<Option> options = new ArrayList<>();
        for (int value : availableValues()) {
            options.add(Option.key(String.valueOf(value)));
        }
        return options;
    }

    int[] availableValues();
}
