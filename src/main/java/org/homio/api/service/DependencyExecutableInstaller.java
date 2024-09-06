package org.homio.api.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.homio.api.Context;
import org.homio.api.state.OnOffType;
import org.homio.api.util.CommonUtils;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Log4j2
@RequiredArgsConstructor
public abstract class DependencyExecutableInstaller {

    protected final Context context;
    protected @Getter String executable;
    private String installedVersion;

    public abstract String getName();

    public @Nullable String getExecutablePath(@NotNull Path execName) {
        if (getVersion() == null) {
            return null;
        }
        if (Files.exists(CommonUtils.getInstallPath().resolve(getName()))) {
            return CommonUtils.getInstallPath().resolve(getName()).resolve(execName).toString();
        }
        // in case if installed externally
        return execName.getFileName().toString();
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
        context.event().fireEvent(getName() + "-dependency-installed", OnOffType.of(true));
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
        context.event().runOnceOnInternetUp("wait-inet-for-install-" + getName(), () -> {
            installDependency(future);
        });

        return future;
    }

    private void installDependency(CompletableFuture<String> future) {
        log.info("Installing dependency: {}", getName());
        context.bgp().runWithProgress("install-" + getName()).onFinally(ex -> {
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
