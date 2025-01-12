package org.homio.api.setting;

import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface SettingPluginOptionsInteger extends SettingPluginInteger, SettingPluginOptions<Integer> {

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.SelectBox;
  }

  @Override
  default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
    List<OptionModel> optionModels = new ArrayList<>();
    for (int value : availableValues()) {
      optionModels.add(OptionModel.key(String.valueOf(value)));
    }
    return optionModels;
  }

  int[] availableValues();
}
