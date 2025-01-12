package org.homio.api.setting;

import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;

public interface SettingPluginToggle extends SettingPlugin<Boolean> {

  @NotNull Icon getIcon();

  @NotNull Icon getToggleIcon();

  @Override
  default @NotNull Class<Boolean> getType() {
    return Boolean.class;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.Toggle;
  }
}
