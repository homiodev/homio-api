package org.homio.api.setting;

import org.homio.api.Context;
import org.homio.api.exception.NotFoundException;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;

public interface SettingPluginOptionsBean<T> extends SettingPluginOptions<T> {

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.SelectBox;
  }

  @Override
  default @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params) {
    return OptionModel.simpleNamelist(context.getBeansOfType(getType()));
  }

  @Override
  default boolean lazyLoad() {
    return true;
  }

  @Override
  default T deserializeValue(Context context, String value) {
    return context.getBeansOfType(getType()).stream().filter(p -> p.getClass().getSimpleName().equals(value)).findAny()
      .orElseThrow(() -> new NotFoundException(
        "Unable to find addon: " + value + " of type: " + getType().getSimpleName()));
  }
}
