package org.homio.api.setting;

import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.homio.api.model.OptionModel.KeyValueEnum;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;

public interface SettingPluginOptionsEnum<T extends Enum<T>> extends SettingPluginOptions<T> {

  @Override
  default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
    if (KeyValueEnum.class.isAssignableFrom(getType())) {
      return OptionModel.list((Class<? extends KeyValueEnum>) getType());
    }
    return allowEmpty() ? OptionModel.enumWithEmpty(getType()) : OptionModel.enumList(getType());
  }

  default boolean allowEmpty() {
    return false;
  }

  default boolean multiSelect() {
    return false;
  }
}
