package org.homio.api.setting.console.header.dynamic;

import org.homio.api.Context;
import org.homio.api.entity.BaseEntity;
import org.homio.api.setting.SettingType;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface DynamicConsoleHeaderContainerSettingPlugin extends ConsoleHeaderSettingPlugin<JSONObject> {

  @Override
  default @NotNull Class<JSONObject> getType() {
    return JSONObject.class;
  }

  @Override
  default @NotNull SettingType getSettingType() {
    return SettingType.SelectBoxButton;
  }

  /**
   * Hide button on UI if no dynamicOptions
   */
  default boolean hideIfNoDynamicSettings() {
    return true;
  }

  @Override
  @NotNull
  default JSONObject getParameters(@NotNull Context context, String value) {
    var params = ConsoleHeaderSettingPlugin.super.getParameters(context, value);
    List<BaseEntity> dynamicSettings = new ArrayList<>();
    DynamicSettingConsumer consumer = new DynamicSettingConsumer() {
      @Override
      public <T> void addDynamicSetting(@NotNull DynamicConsoleHeaderSettingPlugin<T> dynamicSetting) {
        dynamicSettings.add(context.setting().createDynamicSetting(dynamicSetting));
        setValue(context, dynamicSetting, dynamicSetting.getDefaultValue());
      }
    };
    assembleDynamicSettings(context, consumer);
    params.put("dynamicOptions", dynamicSettings);
    if (hideIfNoDynamicSettings()) {
      params.put("hideIfEmptyOptions", true);
    }
    return params;
  }

  /* Fires when user change value for any dynamic option */
  void setValue(@NotNull Context context, @NotNull DynamicConsoleHeaderSettingPlugin<?> setting, @Nullable String value);

  void assembleDynamicSettings(@NotNull Context context, @NotNull DynamicSettingConsumer consumer);

  interface DynamicSettingConsumer {
    <T> void addDynamicSetting(@NotNull DynamicConsoleHeaderSettingPlugin<T> dynamicSetting);
  }
}
