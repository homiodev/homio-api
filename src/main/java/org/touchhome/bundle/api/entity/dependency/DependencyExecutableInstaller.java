package org.touchhome.bundle.api.entity.dependency;

import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.model.ProgressBar;
import org.touchhome.bundle.api.setting.SettingPluginOptionsFileExplorer;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.touchhome.bundle.api.util.Curl.downloadWithProgress;

public interface DependencyExecutableInstaller extends UIActionHandler {

    String getName();

    default void installDependency(EntityContext entityContext, ProgressBar progressBar) throws Exception {
        Path path = installDependencyInternal(entityContext, progressBar);
        if (path != null) {
            entityContext.setting().setValue(getDependencyPluginSettingClass(), path);
        }
        // check dependency installed
        if (isRequireInstallDependencies(entityContext, false)) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled();
        entityContext.event().fireEvent(getName() + "-dependency-installed", true, false);
    }

    boolean isRequireInstallDependencies(EntityContext entityContext, boolean useCacheIfPossible);

    Path installDependencyInternal(EntityContext entityContext, ProgressBar progressBar) throws Exception;

    void afterDependencyInstalled();

    /**
     * Just an utility methodUISidebarButton
     */
    @SneakyThrows
    default Path downloadAndExtract(String url, String archiveType, String folderName, ProgressBar progressBar, Logger log) {
        log.info("Downloading <{}> from url <{}>", folderName, url);
        Path targetFolder = TouchHomeUtils.getInstallPath().resolve(folderName);
        Path archiveFile = targetFolder.resolve(folderName + "." + archiveType);
        downloadWithProgress(url, archiveFile, progressBar);
        progressBar.progress(90, "Unzip files...");
        log.info("Extracting <{}> to path <{}>", archiveFile, targetFolder);
        TouchHomeUtils.unzip(archiveFile, targetFolder, null, progressBar);
        Files.deleteIfExists(archiveFile);
        return targetFolder;
    }

    Class<? extends SettingPluginOptionsFileExplorer> getDependencyPluginSettingClass();

    @Override
    default boolean isEnabled(EntityContext entityContext) {
        return isRequireInstallDependencies(entityContext, true);
    }

    @Override
    default ActionResponseModel handleAction(EntityContext entityContext, JSONObject ignore) {
        if (isRequireInstallDependencies(entityContext, false)) {
            entityContext.bgp().runWithProgress("install-deps-" + getClass().getSimpleName(), false,
                    progressBar -> installDependency(entityContext, progressBar), null,
                    () -> new RuntimeException("INSTALL_DEPENDENCY_IN_PROGRESS"));
        }
        return null;
    }
}
