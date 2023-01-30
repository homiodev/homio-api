package org.touchhome.bundle.api.entity.dependency;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.hardware.other.MachineHardwareRepository;
import org.touchhome.bundle.api.hquery.LinesReader;
import org.touchhome.bundle.api.model.ActionResponseModel;
import org.touchhome.bundle.api.setting.SettingPluginButton;
import org.touchhome.bundle.api.setting.SettingPluginText;
import org.touchhome.bundle.api.ui.action.UIActionHandler;
import org.touchhome.bundle.api.util.TouchHomeUtils;
import org.touchhome.common.model.ProgressBar;
import org.touchhome.common.util.ArchiveUtil;
import org.touchhome.common.util.Curl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

@Log4j2
public abstract class DependencyExecutableInstaller implements UIActionHandler {

    protected Boolean requireInstall;

    /**
     * Just a utility methodUISidebarButton
     */
    @SneakyThrows
    public static Path downloadAndExtract(@NotNull String url, @NotNull String targetFileName,
                                          @NotNull ProgressBar progressBar, @NotNull Logger log) {
        log.info("Downloading <{}> from url <{}>", targetFileName, url);
        Path targetFolder = TouchHomeUtils.getInstallPath();
        Path archiveFile = targetFolder.resolve(targetFileName);
        Curl.downloadWithProgress(url, archiveFile, progressBar);
        progressBar.progress(90, "Unzip files...");
        log.info("Extracting <{}> to path <{}>", archiveFile, targetFolder);
        ArchiveUtil.unzip(archiveFile, targetFolder, null, true, progressBar, ArchiveUtil.UnzipFileIssueHandler.replace);
        Files.deleteIfExists(archiveFile);
        return targetFolder;
    }

    public abstract String getName();

    /**
     * If set - scan DependencyExecutableInstaller and listen when button fires on ui
     * and handle installation
     */
    public abstract @Nullable Class<? extends SettingPluginButton> getInstallButton();

    protected abstract @Nullable Path installDependencyInternal(@NotNull EntityContext entityContext,
                                                                @NotNull ProgressBar progressBar) throws Exception;

    protected void afterDependencyInstalled(@NotNull EntityContext entityContext, @Nullable Path path) {

    }

    public void installDependency(@NotNull EntityContext entityContext, @NotNull ProgressBar progressBar) throws Exception {
        requireInstall = null;
        Path path = installDependencyInternal(entityContext, progressBar);
        if (path != null) {
            entityContext.setting().setValue(getDependencyPluginSettingClass(), path.toString());
        }
        // check dependency installed
        if (isRequireInstallDependencies(entityContext, false)) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled(entityContext, path);
        entityContext.event().fireEvent(getName() + "-dependency-installed", true);
    }

    public synchronized boolean isRequireInstallDependencies(@NotNull EntityContext entityContext, boolean useCacheIfPossible) {
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

    public boolean checkDependencyInstalled(@NotNull EntityContext entityContext, @NotNull MachineHardwareRepository repository) {
        Path targetPath = Paths.get(entityContext.setting().getValue(getDependencyPluginSettingClass()));
        if (Files.isRegularFile(targetPath)) {
            return checkWinDependencyInstalled(repository, targetPath);
        }
        return true;
    }

    public boolean checkWinDependencyInstalled(@NotNull MachineHardwareRepository repository, @NotNull Path targetPath) {
        return !repository.execute(targetPath + " -version").startsWith(getName() + " version");
    }

    public abstract @NotNull Class<? extends SettingPluginText> getDependencyPluginSettingClass();

    public void runService(EntityContext entityContext, Consumer<Process> processConsumer, String entityID) {
        MachineHardwareRepository machineHardwareRepository = entityContext.getBean(MachineHardwareRepository.class);
        if (SystemUtils.IS_OS_LINUX) {
            machineHardwareRepository.startSystemCtl(getName());
        } else {
            Path targetPath = Paths.get(entityContext.setting().getValue(getDependencyPluginSettingClass()));
            Path logFile = targetPath.getParent().resolve("execution-log.log");
            entityContext.bgp().builder(getName() + "-service").linkLogFile(logFile).hideOnUIAfterCancel(false).execute(() -> {
                Process process = Runtime.getRuntime().exec(targetPath.toString());
                entityContext.bgp().executeOnExit(() -> {
                    if (process != null) {
                        process.destroyForcibly();
                    }
                });
                processConsumer.accept(process);
                Thread inputThread =
                        new Thread(new LinesReader(getName() + "inputReader", process.getInputStream(), null, message -> {
                            log.info("[{}]: {}. {}", entityID, getName(), message);
                        }));
                Thread errorThread =
                        new Thread(new LinesReader(getName() + "errorReader", process.getErrorStream(), null, message -> {
                            log.error("[{}]: {}. {}", entityID, getName(), message);
                        }));
                inputThread.start();
                errorThread.start();

                process.waitFor();
                inputThread.interrupt();
                errorThread.interrupt();
            });
        }
    }

    @Override
    public boolean isEnabled(@NotNull EntityContext entityContext) {
        return isRequireInstallDependencies(entityContext, true);
    }

    @Override
    public ActionResponseModel handleAction(@NotNull EntityContext entityContext, @NotNull JSONObject ignore) {
        if (isRequireInstallDependencies(entityContext, false)) {
            entityContext.bgp().runWithProgress("install-deps-" + getClass().getSimpleName(), false,
                    progressBar -> installDependency(entityContext, progressBar), null,
                    () -> new RuntimeException("INSTALL_DEPENDENCY_IN_PROGRESS"));
        }
        return null;
    }
}
