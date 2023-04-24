package org.homio.bundle.api;

import org.homio.bundle.api.ui.field.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for installing different items install program in thread if progressBar is null, run in execution thread if progressBar not null
 */
public interface EntityContextInstall {

    @NotNull InstallContext nodejs();

    @NotNull InstallContext mosquitto();

    @NotNull InstallContext ffmpeg();

    interface InstallContext {

        /**
         * Install nodeJS if require
         *
         * @param version       - specify version or null to install latest
         * @param finishHandler - action firest after finish
         * @throws Exception - exception during installation
         */
        void requireAsync(@Nullable String version, Runnable finishHandler) throws Exception;

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
