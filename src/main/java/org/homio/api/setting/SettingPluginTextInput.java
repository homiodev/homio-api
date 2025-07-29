package org.homio.api.setting;

import static org.homio.api.util.JsonUtils.putOpt;

import org.apache.commons.lang3.tuple.Pair;
import org.homio.api.Context;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface SettingPluginTextInput extends SettingPlugin<String> {

  @Override
  default @NotNull Class<String> getType() {
    return String.class;
  }

  default String getPattern() {
    return null;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.TextInput;
  }

  @Override
  default @NotNull JSONObject getParameters(Context context, String value) {
    JSONObject parameters = SettingPlugin.super.getParameters(context, value);
    putOpt(parameters, "pattern", getPattern());
    Pair<Integer, Integer> validateLength = getValidateLength();
    if (validateLength != null) {
      int min = validateLength.getLeft();
      int max = validateLength.getRight();
      if (min > max) {
        throw new IllegalStateException("Min must be lower than max");
      }
      if (min != max) {
        putOpt(parameters, "pattern", "^.{%s,%s}$".formatted(min, max));
      } else {
        putOpt(parameters, "pattern", "^.{%s}$".formatted(max));
      }
    }
    putOpt(parameters, "requireApply", isHasApplyButton());
    return parameters;
  }

  default Boolean isHasApplyButton() {
    return true;
  }

  default Pair<Integer, Integer> getValidateLength() {
    return null;
  }
}
