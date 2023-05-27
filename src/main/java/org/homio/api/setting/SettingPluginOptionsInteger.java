package org.homio.api.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.homio.api.EntityContext;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.UIFieldType;
import org.json.JSONObject;

public interface SettingPluginOptionsInteger extends SettingPluginInteger, SettingPluginOptions<Integer> {
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
