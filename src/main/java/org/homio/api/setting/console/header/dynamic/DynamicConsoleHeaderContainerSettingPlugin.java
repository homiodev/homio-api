package org.homio.api.setting.console.header.dynamic;

import org.homio.api.setting.SettingType;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public interface DynamicConsoleHeaderContainerSettingPlugin extends ConsoleHeaderSettingPlugin<JSONObject> {

  @Override
  default @NotNull Class<JSONObject> getType() {
    return JSONObject.class;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.SelectBoxButton;
  }
}
