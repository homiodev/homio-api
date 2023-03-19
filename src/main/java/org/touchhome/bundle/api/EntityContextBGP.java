package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingBiFunction;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingRunnable;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.model.HasEntityIdentifier;
import org.touchhome.bundle.api.ui.field.ProgressBar;

import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public interface EntityContextBGP {
    EntityContext getEntityContext();

    /**
     * Create builder to run new thread/scheduler.
     *
     * @param name unique name of thread. cancel thread if already exists
     */
    <T> ScheduleBuilder<T> builder(@NotNull String name);

    /**
     * Await processes to be done
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
     * Run file watchdog. Check file's lastModification updates every 10 seconds and call onUpdateCommand if file had been changed
     *
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
     * Register 'external' threads in context. 'External' mean thread that wasnt created by bgp().builder(...) but eny
     * other new Thread(...) or even java.lang.Process
     */
    void registerThreadsPuller(@NotNull String entityID, @NotNull Consumer<ThreadPuller> threadPullerConsumer);

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

        /**
         * Execute some code with ThreadContext before execution
         */
        ScheduleBuilder<T> tap(Consumer<ThreadContext<T>> handler);

        /**
         * Set delay before first execution.
         */
        ScheduleBuilder<T> delay(@NotNull Duration duration);

        // default false
        ScheduleBuilder<T> hideOnUI(boolean value);

        // default true
        ScheduleBuilder<T> hideOnUIAfterCancel(boolean value);

        // default true
        ScheduleBuilder<T> cancelOnError(boolean value);

        // link log file to be able to read on UI
        ScheduleBuilder<T> linkLogFile(Path logFile);
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

        void setCancelOnError(boolean cancelOnError);

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
         */
        String getTimeToNextSchedule();

        /**
         * Await at most timeout to finish process
         */
        T await(@NotNull Duration timeout) throws InterruptedException, ExecutionException, TimeoutException;

        void onError(@NotNull Consumer<Exception> errorListener);

        /**
         * Add value listener when run/schedule finished.
         *
         * @param name          - unique name of listener
         * @param valueListener - listener: First 2 parameters is value from run/schedule and previous value.
         *                      Return boolean where is remove listener or not after execute
         * @return true if listeners successfully added
         */
        boolean addValueListener(@NotNull String name, @NotNull ThrowingBiFunction<T, T, Boolean, Exception> valueListener);
    }
}
