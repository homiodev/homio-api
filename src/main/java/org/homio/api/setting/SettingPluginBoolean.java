package org.homio.api.setting;

import org.jetbrains.annotations.NotNull;

public interface SettingPluginBoolean extends SettingPlugin<Boolean> {

  default boolean defaultValue() {
    return false;
  }

  @Override
  default @NotNull String getDefaultValue() {
    return Boolean.toString(defaultValue());
  }

  @Override
  default @NotNull Class<Boolean> getType() {
    return Boolean.class;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.Boolean;
  }
}
