package org.homio.api.setting;

import java.nio.file.Files;
import java.nio.file.Path;
import org.homio.api.Context;
import org.homio.api.util.CommonUtils;

public interface SettingPluginJarInstallButton extends SettingPluginButton {

  @Override
  default boolean isDisabled(Context context) {
    return Files.exists(getLocalPath());
  }

  String getJarFileName();

  String getFolder();

  String getServerJarPath();

  default Path getLocalPath() {
    return CommonUtils.getExternalJarClassPath().resolve(getFolder()).resolve(getJarFileName());
  }
}
