package org.touchhome.bundle.api.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.OptionModel;
import org.touchhome.bundle.api.ui.field.UIFieldType;

public interface SettingPluginOptionsInteger
        extends SettingPluginInteger, SettingPluginOptions<Integer> {
    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.SelectBox;
    }

    @Override
    default Collection<OptionModel> getOptions(EntityContext entityContext, JSONObject params) {
        List<OptionModel> optionModels = new ArrayList<>();
        for (int value : availableValues()) {
            optionModels.add(OptionModel.key(String.valueOf(value)));
        }
        return optionModels;
    }

    int[] availableValues();
}
