package org.homio.api.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginOptionsInteger
    extends SettingPluginInteger, SettingPluginOptions<Integer> {

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.SelectBox;
  }

  @Override
  @NotNull
  default JSONObject getParameters(Context context, String value) {
    JSONObject parameters = SettingPluginInteger.super.getParameters(context, value);
    JSONObject optionParameters = SettingPluginOptions.super.getParameters(context, value);
    for (String opKey : optionParameters.keySet()) {
      parameters.put(opKey, optionParameters.get(opKey));
    }
    return parameters;
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
