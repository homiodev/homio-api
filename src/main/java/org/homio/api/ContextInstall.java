package org.homio.api;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import org.homio.api.service.DependencyExecutableInstaller;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for installing different items install program in thread if progressBar is null, run in execution thread if progressBar not null
 */
public interface ContextInstall {

    @NotNull InstallContext nodejs();

    void pipInstall(@NotNull String code);

    @NotNull InstallContext createInstallContext(Class<? extends DependencyExecutableInstaller> installerClass);

    interface InstallContext {

        /**
         * Install software if require
         *
         * @param version       - specify version or null to install latest
         * @param finishHandler - action fires after finish with true/exception or false if already installed
         */
        void requireAsync(@Nullable String version, BiConsumer<Boolean, Exception> finishHandler);

        /**
         * Install program.
         *
         * @param progressBar - progress bar
         * @param version     - program version or null
         * @return this
         * @throws Exception - any exception during installation
         */
        @NotNull InstallContext requireSync(@NotNull ProgressBar progressBar, @Nullable String version) throws Exception;

        /**
         * @return install context version
         */
        @Nullable String getVersion();

        /**
         * Return executable path with relative against base path where execuable installed
         *
         * @param execName - executable name
         * @return path or execName if linux
         */
        @Nullable String getExecutablePath(@NotNull Path execName);
    }

    interface Python {

        void install();
    }
}
