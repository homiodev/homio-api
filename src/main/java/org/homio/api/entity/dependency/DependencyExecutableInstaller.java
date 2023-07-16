package org.homio.api.entity.dependency;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.EntityContext;
import org.homio.hquery.ProgressBar;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public abstract class DependencyExecutableInstaller {

    protected final EntityContext entityContext;
    private String installedVersion;

    public abstract String getName();

    /**
     * @return installed version or null
     */
    protected abstract @Nullable String getInstalledVersion();

    public @Nullable String getExecutablePath(@NotNull String execName) {
        if (getVersion() == null) {
            return null;
        }
        if (IS_OS_LINUX) {
            return execName;
        }
        if (Files.exists(CommonUtils.getInstallPath().resolve(getName()))) {
            return CommonUtils.getInstallPath().resolve(getName()).resolve(execName).toString();
        }
        // in case if installed externally
        return execName;
    }

    protected abstract @Nullable Path installDependencyInternal(@NotNull ProgressBar progressBar, String version) throws Exception;

    public final @Nullable String getVersion() {
        if (installedVersion == null) {
            try {
                installedVersion = trimToNull(getInstalledVersion());
            } catch (Exception ex) {
                log.warn("Unable to fetch {} installed version", getName());
            }
        }
        return installedVersion;
    }

    protected void afterDependencyInstalled(@Nullable Path path) {

    }

    public void installDependency(@NotNull ProgressBar progressBar, @Nullable String version) throws Exception {
        installedVersion = null;
        Path path = installDependencyInternal(progressBar, version);
        // check dependency installed
        if (getVersion() == null) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled(path);
        entityContext.event().fireEvent(getName() + "-dependency-installed", true);
    }
}
