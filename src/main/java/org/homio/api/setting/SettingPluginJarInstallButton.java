package org.homio.api.setting;

import org.homio.api.EntityContext;
import org.homio.api.util.CommonUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public interface SettingPluginJarInstallButton extends SettingPluginButton {

    @Override
    default boolean isDisabled(EntityContext entityContext) {
        return Files.exists(getLocalPath());
    }

    String getJarFileName();

    String getFolder();

    String getServerJarPath();

    default Path getLocalPath() {
        return CommonUtils.getExternalJarClassPath().resolve(getFolder()).resolve(getJarFileName());
    }
}
