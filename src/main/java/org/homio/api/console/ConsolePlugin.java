package org.homio.api.console;

import java.util.Map;
import java.util.Objects;
import org.homio.api.AddonEntrypoint;
import org.homio.api.Context;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.setting.console.header.ConsoleHeaderSettingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/** Uses for implementing page for console tab */
public interface ConsolePlugin<T> extends Comparable<ConsolePlugin<?>> {

  @NotNull
  Context context();

  default @NotNull String getName() {
    return getEntityID();
  }

  default @NotNull String getEntityID() {
    return Objects.toString(AddonEntrypoint.getAddonID(getClass()), getClass().getSimpleName());
  }

  T getValue();

  default @Nullable JSONObject getOptions() {
    return null;
  }

  @NotNull
  RenderType getRenderType();

  /**
   * @return Uses for grouping few addon pages with same parent
   */
  default @Nullable String getParentTab() {
    return null;
  }

  /**
   * @return Uses when need header buttons for whole plugin
   */
  default @Nullable Map<String, Class<? extends ConsoleHeaderSettingPlugin<?>>> getHeaderActions() {
    return null;
  }

  /**
   * @return Draw console titles in such order
   */
  default int order() {
    return 0;
  }

  @Override
  default int compareTo(@NotNull ConsolePlugin consolePlugin) {
    return Integer.compare(order(), consolePlugin.order());
  }

  // hide from ui if not enabled
  default boolean isEnabled() {
    return true;
  }

  // enable refresh interval select-box
  default boolean hasRefreshIntervalSetting() {
    return true;
  }

  default @Nullable ActionResponseModel executeAction(
      @NotNull String entityID, @NotNull JSONObject metadata) throws Exception {
    return null;
  }

  default void assembleOptions(JSONObject options) {}

  enum RenderType {
    lines,
    comm,
    table,
    string,
    editor,
    tree,
    frame
  }
}
