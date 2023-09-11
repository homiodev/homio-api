package org.homio.api.service;

import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.EntityContext;
import org.homio.api.util.CommonUtils;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public abstract class DependencyExecutableInstaller {

    protected final EntityContext entityContext;
    private String installedVersion;
    protected @Getter String executable;

    public abstract String getName();

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

    public void installDependency(@NotNull ProgressBar progressBar, @Nullable String version) throws Exception {
        installedVersion = null;
        installDependencyInternal(progressBar, version);
        // check dependency installed
        if (getVersion() == null) {
            throw new RuntimeException("Something went wrong after install dependency. Executable file still required");
        }
        progressBar.progress(99, "Installing finished");
        afterDependencyInstalled();
        entityContext.event().fireEvent(getName() + "-dependency-installed", true);
    }

    public String installLatest() throws ExecutionException, InterruptedException {
        String version = getInstalledVersion();
        if (version != null) {
            return version;
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        installDependency(future);
        return future.get();
    }

    public CompletableFuture<String> installLatestAsync() {
        String version = getInstalledVersion();
        if (version != null) {
            return CompletableFuture.completedFuture(version);
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        entityContext.event().runOnceOnInternetUp("wait-inet-for-install-" + getName(), () -> {
            installDependency(future);
        });

        return future;
    }

    private void installDependency(CompletableFuture<String> future) {
        entityContext.bgp().runWithProgress("install-" + getName()).onFinally(ex -> {
            if (ex != null) {
                log.error("Unable to install {}", getName(), ex);
                future.completeExceptionally(ex);
            } else {
                log.info("{} service successfully installed", getName());
                future.complete(getVersion());
            }
        }).execute(progressBar -> {
            installDependency(progressBar, null);
        });
    }

    /**
     * @return installed version or null
     */
    protected abstract @Nullable String getInstalledVersion();

    protected abstract void installDependencyInternal(@NotNull ProgressBar progressBar, String version) throws Exception;

    protected void afterDependencyInstalled() {
    }
}
