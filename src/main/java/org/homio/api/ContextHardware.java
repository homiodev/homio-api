package org.homio.api;

import java.util.ArrayList;
import org.homio.hquery.ProgressBar;
import org.homio.hquery.api.HQueryMaxWaitTimeout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ContextHardware {

    @NotNull Context context();

    @NotNull ContextNetwork network();

    @NotNull String execute(@NotNull String command);

    @NotNull String executeNoErrorThrow(@NotNull String command, int maxSecondsTimeout,
        @Nullable ProgressBar progressBar);

    @NotNull ArrayList<String> executeNoErrorThrowList(@NotNull String command, int maxSecondsTimeout,
        @Nullable ProgressBar progressBar);

    @NotNull String execute(@NotNull String command, @Nullable ProgressBar progressBar);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    @NotNull String execute(@NotNull String command, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        @Nullable ProgressBar progressBar);

    boolean isSoftwareInstalled(@NotNull String soft);

    @NotNull ContextHardware installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout);

    @NotNull ContextHardware installSoftware(@NotNull String soft, @HQueryMaxWaitTimeout int maxSecondsTimeout,
        @Nullable ProgressBar progressBar);

    @NotNull ContextHardware enableSystemCtl(@NotNull String soft);

    @NotNull ContextHardware startSystemCtl(@NotNull String soft);

    default boolean isSystemCtlExists(@NotNull String soft) {
        return "active".equals(execute("systemctl is-active mosquitto"));
    }

    void stopSystemCtl(@NotNull String soft);

    int getServiceStatus(@NotNull String serviceName);

    void reboot();

    @NotNull ProcessStat getProcessStat(long pid);

    /**
     * Enable and start soft
     *
     * @param soft - system service
     * @return this
     */
    default ContextHardware enableAndStartSystemCtl(@NotNull String soft) {
        enableSystemCtl(soft);
        startSystemCtl(soft);
        return this;
    }

    default ContextHardware update() {
        execute("$PM update");
        return this;
    }

    interface ProcessStat {
        // get cpu usage in % by process
        double getCpuUsage();

        // get memory usage in % by process
        double getMemUsage();

        // get memory used in bytes
        long getMem();
    }
}
