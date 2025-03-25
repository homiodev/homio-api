package org.homio.api.setting;

import org.homio.api.Context;
import org.homio.api.model.OptionModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collection;

import static org.homio.api.util.JsonUtils.putOpt;

public interface SettingPluginOptions<T> extends SettingPlugin<T> {

  @NotNull Collection<OptionModel> getOptions(Context context, JSONObject params);

  default boolean allowEmpty() {
    return false;
  }

  default boolean lazyLoad() {
    return false;
  }

  default boolean rawInput() {
    return false;
  }

  default boolean multiSelect() {
    return false;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return viewAsButton() ? SettingType.SelectBoxButton : SettingType.SelectBox;
  }

  default boolean viewAsButton() {
    return false;
  }

  // specify max width of rendered ui item
  default @Nullable Integer getMaxWidth() {
    return null;
  }

  default @Nullable Integer getMinWidth() {
    return null;
  }

  default @NotNull JSONObject getParameters(Context context, String value) {
    JSONObject parameters = SettingPlugin.super.getParameters(context, value);
    if (!viewAsButton()) {
      putOpt(parameters, "maxWidth", getMaxWidth());
      putOpt(parameters, "minWidth", getMinWidth());
    }
    if (lazyLoad()) {
      parameters.put("lazyLoad", true);
    }
    if (rawInput()) {
      parameters.put("rawInput", true);
    }
    return parameters;
  }
}
