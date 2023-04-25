package org.homio.bundle.api;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import org.homio.bundle.hquery.api.HQueryMaxWaitTimeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextHardware {

    @NotNull EntityContext getEntityContext();

    @NotNull String execute(String command);

    @NotNull String executeNoErrorThrow(@NotNull String command, int maxSecondsTimeout,
        @Nullable BiConsumer<Double, String> progressBar);

    @NotNull ArrayList<String> executeNoErrorThrowList(@NotNull String command, int maxSecondsTimeout,
        @Nullable BiConsumer<Double, String> progressBar);

    @NotNull String execute(@NotNull String command, @Nullable BiConsumer<Double, String> progressBar);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        @Nullable BiConsumer<Double, String> progressBar);

    boolean isSoftwareInstalled(@NotNull String soft);

    void installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    void installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        BiConsumer<Double, String> progressBar);

    void enableSystemCtl(@NotNull String soft);

    void startSystemCtl(@NotNull String soft);

    void stopSystemCtl(@NotNull String soft);

    @NotNull String getHostname();

    int getServiceStatus(@NotNull String serviceName);

    void reboot();
}
