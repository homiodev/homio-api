package org.homio.bundle.api.setting;

import java.nio.file.Files;
import java.nio.file.Path;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.util.TouchHomeUtils;

public interface SettingPluginJarInstallButton extends SettingPluginButton {

    @Override
    default boolean isDisabled(EntityContext entityContext) {
        return Files.exists(getLocalPath());
    }

    String getJarFileName();

    String getFolder();

    String getServerJarPath();

    default Path getLocalPath() {
        return TouchHomeUtils.getExternalJarClassPath().resolve(getFolder()).resolve(getJarFileName());
    }
}
