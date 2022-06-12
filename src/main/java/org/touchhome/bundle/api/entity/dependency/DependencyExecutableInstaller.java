package org.touchhome.bundle.api.entity.dependency;

import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hardware.other.MachineHardwareRepository;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.setting.SettingPluginOptionsFileExplorer;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.model.ProgressBar;
import org.touchhome.common.util.ArchiveUtil;
import org.touchhome.common.util.Curl;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DependencyExecutableInstaller implements UIActionHandler {

    protected Boolean requireInstall;

    public abstract String getName();

    /**
     * If set - scan DependencyExecutableInstaller and listen when button fires on ui
     * and handle installation
     */
    public abstract Class<? extends SettingPluginButton> getInstallButton();

    protected abstract Path installDependencyInternal(EntityContext entityContext, ProgressBar progressBar) throws Exception;

    protected void afterDependencyInstalled(EntityContext entityContext, Path path) {

    }

    public void installDependency(EntityContext entityContext, ProgressBar progressBar) throws Exception {
        requireInstall = null;
        Path path = installDependencyInternal(entityContext, progressBar);
        if (path != null) {
            entityContext.setting().setValue(getDependencyPluginSettingClass(), path);
        }
        // check dependency installed
        if (isRequireInstallDependencies(entityContext, false)) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled(entityContext, path);
        entityContext.event().fireEvent(getName() + "-dependency-installed", true, false);
    }

    public synchronized boolean isRequireInstallDependencies(EntityContext entityContext, boolean useCacheIfPossible) {
        if (requireInstall == null || !useCacheIfPossible) {
            requireInstall = true;
            MachineHardwareRepository repository = entityContext.getBean(MachineHardwareRepository.class);
            if (repository.isSoftwareInstalled(getName())) {
                requireInstall = false;
            } else {
                requireInstall = checkDependencyInstalled(entityContext, repository);
            }
        }
        return requireInstall;
    }

    public boolean checkDependencyInstalled(EntityContext entityContext, MachineHardwareRepository repository) {
        Path targetPath = entityContext.setting().getValue(getDependencyPluginSettingClass());
        if (Files.isRegularFile(targetPath)) {
            return checkWinDependencyInstalled(repository, targetPath);
        }
        return true;
    }

    public boolean checkWinDependencyInstalled(MachineHardwareRepository repository, Path targetPath) {
        return !repository.execute(targetPath + " -version").startsWith(getName() + " version");
    }

    /**
     * Just an utility methodUISidebarButton
     */
    @SneakyThrows
    public Path downloadAndExtract(String url, String archiveType, String folderName, ProgressBar progressBar, Logger log) {
        log.info("Downloading <{}> from url <{}>", folderName, url);
        Path targetFolder = TouchHomeUtils.getInstallPath().resolve(folderName);
        Path archiveFile = targetFolder.resolve(folderName + "." + archiveType);
        Curl.downloadWithProgress(url, archiveFile, progressBar);
        progressBar.progress(90, "Unzip files...");
        log.info("Extracting <{}> to path <{}>", archiveFile, targetFolder);
        ArchiveUtil.unzip(archiveFile, targetFolder, null, progressBar, ArchiveUtil.UnzipFileIssueHandler.replace);
        Files.deleteIfExists(archiveFile);
        return targetFolder;
    }

    public abstract Class<? extends SettingPluginOptionsFileExplorer> getDependencyPluginSettingClass();

    @Override
    public boolean isEnabled(EntityContext entityContext) {
        return isRequireInstallDependencies(entityContext, true);
    }

    @Override
    public ActionResponseModel handleAction(EntityContext entityContext, JSONObject ignore) {
        if (isRequireInstallDependencies(entityContext, false)) {
            entityContext.bgp().runWithProgress("install-deps-" + getClass().getSimpleName(), false,
                    progressBar -> installDependency(entityContext, progressBar), null,
                    () -> new RuntimeException("INSTALL_DEPENDENCY_IN_PROGRESS"));
        }
        return null;
    }
}
