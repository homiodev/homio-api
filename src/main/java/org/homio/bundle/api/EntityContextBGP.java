package org.homio.bundle.api;

import com.pivovarit.function.ThrowingBiFunction;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingRunnable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.Logger;
import org.homio.bundle.api.model.HasEntityIdentifier;
import org.homio.bundle.api.setting.SettingPlugin;
import org.homio.bundle.api.ui.field.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public interface EntityContextBGP {
    EntityContext getEntityContext();

    /**
     * Create builder to run new thread/scheduler.
     *
     * @param name unique name of thread. cancel thread if already exists
     * @param <T> -
     * @return -
     */
    <T> ScheduleBuilder<T> builder(@NotNull String name);

    /**
     * Await processes to be done
     *
     * @param name          - process name
     * @param maxTimeToWait - time to wait
     * @param processes     - list of processes to wait finished
     * @param logger        - logger
     * @param finallyBlock  - block to execute when all processes finished
     * @param <T> -
     * @return this
     */
    default <T> ThreadContext<Map<String, T>> awaitProcesses(@NotNull String name, Duration maxTimeToWait,
                                                             @NotNull List<ThreadContext<T>> processes,
                                                             @NotNull Logger logger,
                                                             @Nullable Consumer<Map<String, T>> finallyBlock) {
        ScheduleBuilder<Map<String, T>> builder = builder(name);
        return builder.execute(arg -> {
            Map<String, T> result = new HashMap<>();
            for (ThreadContext<T> process : processes) {
                try {
                    result.put(process.getName(), process.await(maxTimeToWait));
                } catch (Exception ex) {
                    result.put(process.getName(), null);
                    logger.error("Error while await <{}> for finish. Fire process termination", process.getName());
                    process.cancel();
                }
            }
            if (finallyBlock != null) {
                finallyBlock.accept(result);
            }
            return result;
        });
    }

    /**
     * Start service forever
     *
     * @param entityContext   -
     * @param processConsumer -
     * @param name            - service name to start
     * @param settingClass    - setting class that store absolute path to service executable file. May be null for linux or in case if path equal to
     *                        getInstallPath() + '/' + name
     * @param <T>             setting class type
     */
    <T> void runService(
        @NotNull EntityContext entityContext,
        @NotNull Consumer<Process> processConsumer,
        @NotNull String name,
        @NotNull Class<? extends SettingPlugin<T>> settingClass);

    /**
     * Run file watchdog. Check file's lastModification updates every 10 seconds and call onUpdateCommand if file had been changed
     *
     * @param file            - run file to check for changes
     * @param key             - distinguish key
     * @param onUpdateCommand - command to execute on update
     * @return -
     * @throws IllegalArgumentException if file not readable
     */
    ThreadContext<Void> runFileWatchdog(@NotNull Path file, String key, @NotNull ThrowingRunnable<Exception> onUpdateCommand)
            throws IllegalArgumentException;

    default ThreadContext<Void> runWithProgress(@NotNull String key, boolean cancellable,
                                                @NotNull ThrowingConsumer<ProgressBar, Exception> command) {
        return runWithProgress(key, cancellable, command, null, null);
    }

    default <R> ThreadContext<R> runWithProgressAndGet(@NotNull String key, boolean cancellable,
                                                       @NotNull ThrowingFunction<ProgressBar, R, Exception> command) {
        return runWithProgressAndGet(key, cancellable, command, null, null);
    }

    default <R> ThreadContext<R> runWithProgressAndGet(@NotNull String key, boolean cancellable,
                                                       @NotNull ThrowingFunction<ProgressBar, R, Exception> command,
                                                       @NotNull Consumer<Exception> finallyBlock) {
        return runWithProgressAndGet(key, cancellable, command, finallyBlock, null);
    }

    default ThreadContext<Void> runWithProgress(@NotNull String key,
                                                boolean cancellable,
                                                @NotNull ThrowingConsumer<ProgressBar, Exception> command,
                                                @NotNull Consumer<Exception> finallyBlock) {
        return runWithProgress(key, cancellable, command, finallyBlock, null);
    }

    default ThreadContext<Void> runWithProgress(@NotNull String key, boolean cancellable,
                                                @NotNull ThrowingConsumer<ProgressBar, Exception> command,
                                                @Nullable Consumer<Exception> finallyBlock,
                                                @Nullable Supplier<RuntimeException> throwIfExists) {
        return runWithProgressAndGet(key, cancellable, progressBar -> {
            command.accept(progressBar);
            return null;
        }, finallyBlock, throwIfExists);
    }

    default <R> ThreadContext<R> runWithProgressAndGet(@NotNull String key, boolean cancellable,
                                                       @NotNull ThrowingFunction<ProgressBar, R, Exception> command,
                                                       @Nullable Consumer<Exception> finallyBlock,
                                                       @Nullable Supplier<RuntimeException> throwIfExists) {
        if (throwIfExists != null && isThreadExists(key, true)) {
            RuntimeException exception = throwIfExists.get();
            if (exception != null) {
                throw exception;
            }
        }
        ScheduleBuilder<R> builder = builder(key);
        return builder.execute(arg -> {
            ProgressBar progressBar =
                    (progress, message) -> getEntityContext().ui().progress(key, progress, message, cancellable);
            progressBar.progress(0, key);
            Exception exception = null;
            try {
                return command.apply(progressBar);
            } catch (Exception ex) {
                exception = ex;
                throw ex;
            } finally {
                progressBar.done();
                if (finallyBlock != null) {
                    finallyBlock.accept(exception);
                }
            }
        });
    }

    boolean isThreadExists(@NotNull String name, boolean checkOnlyRunningThreads);

    void cancelThread(@NotNull String name);

    /**
     * Register 'external' threads in context. 'External' mean thread that wasn't created by bgp().builder(...) but eny
     * other new Thread(...) or even java.lang.Process
     * @param id - distinguish id
     * @param threadPullerConsumer - thread object consumer
     */
    void registerThreadsPuller(@NotNull String id, @NotNull Consumer<ThreadPuller> threadPullerConsumer);

    <P extends HasEntityIdentifier, T> void runInBatch(@NotNull String batchName,
                                                       @Nullable Duration maxTerminateTimeout,
                                                       @NotNull Collection<P> taskItems,
                                                       @NotNull Function<P, Callable<T>> callableProducer,
                                                       @NotNull Consumer<Integer> progressConsumer,
                                                       @NotNull Consumer<List<T>> finallyProcessBlockHandler);

    @NotNull <T> List<T> runInBatchAndGet(@NotNull String batchName,
                                          @Nullable Duration maxTerminateTimeout,
                                          int threadsCount,
                                          @NotNull Map<String, Callable<T>> runnableTasks,
                                          @NotNull Consumer<Integer> progressConsumer);

    void executeOnExit(Runnable runnable);

    interface ScheduleBuilder<T> {
        default ThreadContext<T> execute(@NotNull ThrowingFunction<ThreadContext<T>, T, Exception> command) {
            return execute(command, true);
        }

        default ThreadContext<Void> execute(@NotNull ThrowingRunnable<Exception> command) {
            return execute(command, true);
        }

        ThreadContext<T> execute(@NotNull ThrowingFunction<ThreadContext<T>, T, Exception> command, boolean start);

        ThreadContext<Void> execute(@NotNull ThrowingRunnable<Exception> command, boolean start);

        ScheduleBuilder<T> interval(@NotNull String cron);

        ScheduleBuilder<T> interval(@NotNull Duration duration);

        ScheduleBuilder<T> throwOnError(boolean value);

        ScheduleBuilder<T> onError(@NotNull Consumer<Exception> errorListener);

        ScheduleBuilder<T> metadata(@NotNull String key, @NotNull Object value);

        /**
         * Execute some code with ThreadContext before execution
         * @param handler - consumer to execute
         * @return ScheduleBuilder
         */
        ScheduleBuilder<T> tap(Consumer<ThreadContext<T>> handler);

        /**
         * Set delay before first execution.
         *
         * @param duration - wait timeout
         * @return ScheduleBuilder
         */
        ScheduleBuilder<T> delay(@NotNull Duration duration);

        /**
         * Specify that need path requested authenticated user to background process thread
         *
         * @return this
         */
        ScheduleBuilder<T> auth();

        // default false
        ScheduleBuilder<T> hideOnUI(boolean value);

        // default true
        ScheduleBuilder<T> hideOnUIAfterCancel(boolean value);

        // default true
        ScheduleBuilder<T> cancelOnError(boolean value);

        // link log file to be able to read on UI
        ScheduleBuilder<T> linkLogFile(Path logFile);

        /**
         * Add value listener when run/schedule finished.
         *
         * @param name          - unique name of listener
         * @param valueListener - listener: First 2 parameters is value from run/schedule and previous value.
         *                      Return boolean where is remove listener or not after execute
         * @return true if listeners successfully added
         */
        ScheduleBuilder<T> valueListener(@NotNull String name, @NotNull ThrowingBiFunction<T, T, Boolean, Exception> valueListener);
    }

    interface ThreadPuller {
        @NotNull ThreadPuller addThread(@NotNull String name, @Nullable String description, @NotNull Date creationTime,
                                        @Nullable String state, @Nullable String errorMessage, @Nullable String bigDescription);

        @NotNull ThreadPuller addScheduler(@NotNull String name, @Nullable String description, @NotNull Date creationTime,
                                           @Nullable String state, @Nullable String errorMessage, @Nullable Duration period,
                                           int runCount,
                                           @Nullable String bigDescription);
    }

    interface ThreadContext<T> {
        @NotNull String getName();

        @NotNull String getState();

        void setState(@NotNull String state);

        @Nullable Object getMetadata(String key);

        void setMetadata(@NotNull String key, @NotNull Object value);

        @Nullable String getDescription();

        void setDescription(@NotNull String description);

        boolean isStopped();

        void cancel();

        /**
         * Reset thread. Set delay to initial state. If thread already finished - start it again. Start if not started yet
         */
        void reset();

        /**
         * Next schedule call as string
         * @return next time to call
         */
        String getTimeToNextSchedule();

        /**
         * Await at most timeout to finish process
         * @param timeout - duration to wait
         * @return response returned by thread
         * @throws Exception -
         */
        T await(@NotNull Duration timeout) throws Exception;

        boolean addValueListener(@NotNull String name, @NotNull ThrowingBiFunction<T, T, Boolean, Exception> valueListener);

        boolean removeValueListener(@NotNull String name);
    }
}
