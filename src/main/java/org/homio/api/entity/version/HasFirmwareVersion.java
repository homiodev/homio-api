package org.homio.api.entity.version;

import java.util.List;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.OptionModel;
import org.homio.api.ui.field.UIFieldNoReadDefaultValue;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Interface for entities that has specific program to execute and has version */
public interface HasFirmwareVersion {

  @UIFieldNoReadDefaultValue
  @Nullable
  String getFirmwareVersion();

  default @Nullable String getFirmwareVersionColor() {
    return null;
  }

  /**
   * Return last available firmware version if entity able to update to it
   *
   * @return last available version
   */
  default @Nullable String getLastFirmwareVersion() {
    return null;
  }

  default @Nullable String getFirmwareVersionReadme(@NotNull String version) {
    return null;
  }

  default @Nullable List<OptionModel> getNewAvailableVersion() {
    return null;
  }

  default boolean isFirmwareUpdating() {
    return false;
  }

  // fire update in thread
  default ActionResponseModel update(@NotNull ProgressBar progressBar, @NotNull String version)
      throws Exception {
    throw new IllegalStateException("Must be implemented. Calls if fired on UI 'Update button'");
  }
}
