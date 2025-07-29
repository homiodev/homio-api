package org.homio.api.setting;

import static org.homio.api.util.JsonUtils.putOpt;

import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginSlider extends SettingPluginInteger {

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.Slider;
  }

  default Integer getStep() {
    return null;
  }

  default String getHeader() {
    return null;
  }

  @Override
  default @NotNull JSONObject getParameters(Context context, String value) {
    JSONObject parameters = SettingPluginInteger.super.getParameters(context, value);
    putOpt(parameters, "step", getStep());
    putOpt(parameters, "header", getHeader());
    return parameters;
  }
}
