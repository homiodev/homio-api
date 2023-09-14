package org.homio.api;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.homio.api.service.DependencyExecutableInstaller;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for installing different items install program in thread if progressBar is null, run in execution thread if progressBar not null
 */
public interface EntityContextInstall {

    @NotNull InstallContext nodejs();

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

        @Nullable String getPath(@NotNull String execName);
    }
}
