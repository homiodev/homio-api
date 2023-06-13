package org.homio.api;

import java.util.ArrayList;
import org.homio.hquery.HQueryProgressBar;
import org.homio.hquery.api.HQueryMaxWaitTimeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextHardware {

    @NotNull EntityContext getEntityContext();

    @NotNull String execute(String command);

    @NotNull String executeNoErrorThrow(@NotNull String command, int maxSecondsTimeout,
        @Nullable HQueryProgressBar progressBar);

    @NotNull ArrayList<String> executeNoErrorThrowList(@NotNull String command, int maxSecondsTimeout,
        @Nullable HQueryProgressBar progressBar);

    @NotNull String execute(@NotNull String command, @Nullable HQueryProgressBar progressBar);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        @Nullable HQueryProgressBar progressBar);

    boolean isSoftwareInstalled(@NotNull String soft);

    EntityContextHardware installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    EntityContextHardware installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        HQueryProgressBar progressBar);

    EntityContextHardware enableSystemCtl(@NotNull String soft);

    EntityContextHardware startSystemCtl(@NotNull String soft);

    void stopSystemCtl(@NotNull String soft);

    @NotNull String getHostname();

    int getServiceStatus(@NotNull String serviceName);

    void reboot();

    /**
     * Enable and start soft
     *
     * @param soft - system service
     * @return this
     */
    default EntityContextHardware enableAndStartSystemCtl(@NotNull String soft) {
        enableSystemCtl(soft);
        startSystemCtl(soft);
        return this;

    }

    default EntityContextHardware update() {
        execute("$PM update");
        return this;
    }
}
