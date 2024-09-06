package org.homio.api;

import com.pivovarit.function.*;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Logger;
import org.homio.api.entity.HasStatusAndMsg;
import org.homio.api.entity.device.DeviceBaseEntity;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.service.EntityService;
import org.homio.hquery.ProgressBar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.homio.api.util.CommonUtils.getErrorMessage;

@SuppressWarnings("unused")
public interface ContextBGP {

    static boolean cancel(ThreadContext<?> threadContext) {
        if (threadContext != null) {
            threadContext.cancel();
            return true;
        }
        return false;
    }

    static boolean cancel(ProcessContext processContext) {
        if (processContext != null) {
            processContext.cancel(true);
            return true;
        }
        return false;
    }

    /**
     * Add handler that executes once per minute
     */
    void addLowPriorityRequest(String key, ThrowingRunnable<Exception> handler);

    void removeLowPriorityRequest(String key);

    // run ping for ip address every minute
    void ping(@NotNull String discriminator, @NotNull String ipAddress, @NotNull Consumer<Boolean> availableStatus);

    void unPing(@NotNull String discriminator, @Nullable String ipAddress);

    @NotNull
    Context context();

    /**
     * Simple create new thread and call runnable
     *
     * @param runnable code to run
     */
    default void execute(@NotNull ThrowingRunnable<Exception> runnable) {
        execute(null, runnable);
    }

    void execute(@Nullable Duration delay, @NotNull ThrowingRunnable<Exception> runnable);

    /**
     * Create builder to run new thread/scheduler.
     *
     * @param name unique name of thread. cancel thread if already exists
     * @param <T>  -
     * @return -
     */
    @NotNull
    <T> ScheduleBuilder<T> builder(@NotNull String name);

    @NotNull
    ProcessBuilder processBuilder(@NotNull String name);

    default @NotNull ProcessBuilder processBuilder(@NotNull DeviceBaseEntity entity, @NotNull Logger log) {
        return
                processBuilder(entity.getEntityID())
                        .onStarted(t -> entity.setStatusOnline())
                        .attachLogger(log)
                        .attachEntityStatus(entity)
                        .onFinished((ex, responseCode) -> {
                            if (ex != null) {
                                log.error("[{}]: Error while start {} dashboard {}",
                                        entity.getEntityID(), entity.getTitle(), getErrorMessage(ex));
                            } else {
                                log.warn("[{}]: {} finished with status: {}",
                                        entity.getEntityID(), entity.getTitle(), responseCode);
                            }
                            if (entity instanceof EntityService<?> es) {
                                es.destroyService(ex);
                            }
                        })
                        .setErrorLoggerOutput(msg -> log.error("[{}]: {}: {}", entity.getEntityID(), entity.getTitle(), msg))
                        .setInputLoggerOutput(msg -> log.info("[{}]: {}: {}", entity.getEntityID(), entity.getTitle(), msg));
    }

    /**
     * Await processes to be done
     *
     * @param name          - process name
     * @param maxTimeToWait - time to wait
     * @param processes     - list of processes to wait finished
     * @param logger        - logger
     * @param finallyBlock  - block to execute when all processes finished
     * @param <T>           -
     * @return this
     */
    default @NotNull <T> ThreadContext<Map<String, T>> awaitProcesses(@NotNull String name, Duration maxTimeToWait,
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
     * @param file            - run file to check for changes
     * @param key             - distinguish key
     * @param onUpdateCommand - command to execute on update
     * @return -
     * @throws IllegalArgumentException if file not readable
     */
    @NotNull
    ThreadContext<Void> runFileWatchdog(@NotNull Path file, String key, @NotNull ThrowingRunnable<Exception> onUpdateCommand)
            throws IllegalArgumentException;

    /**
     * Run directory watchdog for specific events.
     *
     * @param dir             - dir to watch
     * @param onUpdateCommand - handler
     * @param eventsToListen  - default is: StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY
     * @return - thread context
     * @throws IllegalArgumentException if directory not exists or dir already under watching
     */
    @NotNull
    ThreadContext<Void> runDirectoryWatchdog(@NotNull Path dir, @NotNull ThrowingConsumer<WatchEvent<Path>, Exception> onUpdateCommand,
                                             Kind<?>... eventsToListen);

    /**
     * Create builder that consume progress bar. Every progress bar update reflects on UI.
     *
     * @param key - unique bgp builder key
     * @return - progress builder
     */
    default @NotNull ProgressBuilder runWithProgress(@NotNull String key) {
        return runWithProgress(key, false);
    }

    @NotNull
    ProgressBuilder runWithProgress(@NotNull String key, boolean cancellable);

    boolean isThreadExists(@NotNull String name, boolean checkOnlyRunningThreads);

    void cancelThread(@NotNull String name);

    /**
     * Register 'external' threads in context. 'External' mean thread that wasn't created by bgp().builder(...) but eny other new Thread(...) or even
     * java.lang.Process
     *
     * @param id                   - distinguish id
     * @param threadPullerConsumer - thread object consumer
     */
    void registerThreadsPuller(@NotNull String id, @NotNull Consumer<ThreadPuller> threadPullerConsumer);

    <P extends HasEntityIdentifier, T> void runInBatch(@NotNull String batchName,
                                                       @Nullable Duration maxTerminateTimeout,
                                                       @NotNull Collection<P> taskItems,
                                                       @NotNull Function<P, Callable<T>> callableProducer,
                                                       @NotNull Consumer<Integer> progressConsumer,
                                                       @NotNull Consumer<List<T>> finallyProcessBlockHandler);

    @NotNull
    <T> List<T> runInBatchAndGet(@NotNull String batchName,
                                 @Nullable Duration maxTerminateTimeout,
                                 int threadsCount,
                                 @NotNull Map<String, Callable<T>> runnableTasks,
                                 @NotNull Consumer<Integer> progressConsumer);

    void executeOnExit(@NotNull String name, @NotNull ThrowingRunnable<Exception> runnable);

    interface ProgressBuilder {

        // default - false
        @NotNull
        ProgressBuilder setCancellable(boolean cancellable);

        // default - true
        @NotNull
        ProgressBuilder setLogToConsole(boolean value);

        /**
         * Throw error if process already exists
         *
         * @param ex - error to throw if process already exists
         * @return this
         */
        @NotNull
        ProgressBuilder setErrorIfExists(@Nullable Exception ex);

        @NotNull
        ProgressBuilder onFinally(@Nullable Consumer<Exception> finallyBlock);

        @NotNull
        default ProgressBuilder onFinally(@Nullable Runnable finallyBlock) {
            return onFinally(ignore -> {
                if (finallyBlock != null) {
                    finallyBlock.run();
                }
            });
        }

        @NotNull
        ProgressBuilder onError(@Nullable Runnable errorBlock);

        @NotNull
        default ThreadContext<Void> execute(@NotNull ThrowingConsumer<ProgressBar, Exception> command) {
            return execute(progressBar -> {
                command.accept(progressBar);
                return null;
            });
        }

        @SneakyThrows
        default CompletableFuture<Void> executeSync(@NotNull ThrowingConsumer<ProgressBar, Exception> command) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            execute(progressBar -> {
                command.accept(progressBar);
                future.complete(null);
                return null;
            });
            return future;
        }

        @NotNull
        <R> ThreadContext<R> execute(@NotNull ThrowingFunction<ProgressBar, R, Exception> command);
    }

    interface ProcessBuilder {

        @NotNull
        ProcessBuilder setInputLoggerOutput(@Nullable Consumer<String> inputConsumer);

        @NotNull
        ProcessBuilder setErrorLoggerOutput(@Nullable Consumer<String> errorConsumer);

        @NotNull
        ProcessBuilder onStarted(@NotNull ThrowingConsumer<ProcessContext, Exception> startedHandler);

        default @NotNull ProcessBuilder onStarted(@NotNull ThrowingRunnable<Exception> startedHandler) {
            return onStarted(processContext -> startedHandler.run());
        }

        // finish handler on error or on regular finish process. finishHandler get ex
        @NotNull
        ProcessBuilder onFinished(@NotNull ThrowingBiConsumer<@Nullable Exception, @NotNull Integer, Exception> finishHandler);

        @NotNull
        ProcessBuilder attachLogger(@NotNull Logger log);

        @NotNull
        ProcessBuilder attachEntityStatus(@NotNull HasStatusAndMsg entity);

        @NotNull
        ProcessBuilder workingDir(@NotNull Path dir);

        @NotNull
        ProcessContext execute(@NotNull String... command);
    }

    interface ScheduleBuilder<T> {

        @NotNull
        default ThreadContext<T> execute(@NotNull ThrowingFunction<ThreadContext<T>, T, Exception> command) {
            return execute(command, true);
        }

        @NotNull
        default ThreadContext<Void> execute(@NotNull ThrowingRunnable<Exception> command) {
            return execute(command, true);
        }

        @NotNull
        ThreadContext<T> execute(@NotNull ThrowingFunction<ThreadContext<T>, T, Exception> command, boolean start);

        @NotNull
        ThreadContext<Void> execute(@NotNull ThrowingRunnable<Exception> command, boolean start);

        @NotNull
        ScheduleBuilder<T> interval(@NotNull String cron);

        @NotNull
        ScheduleBuilder<T> interval(@NotNull Duration duration);

        default @NotNull ScheduleBuilder<T> intervalWithDelay(@NotNull Duration duration) {
            delay(duration);
            return interval(duration);
        }

        @NotNull
        ScheduleBuilder<T> throwOnError(boolean value);

        @NotNull
        ScheduleBuilder<T> onError(@NotNull Consumer<Exception> errorListener);

        /**
         * Executes once on single thread finished or every run in case of interval
         *
         * @param runnable - code to run
         * @return this
         */
        @NotNull
        ScheduleBuilder<T> onFinally(@NotNull Runnable runnable);

        @NotNull
        ScheduleBuilder<T> metadata(@NotNull String key, @NotNull Object value);

        /**
         * Execute some code with ThreadContext before execution
         *
         * @param handler - consumer to execute
         * @return ScheduleBuilder
         */
        @NotNull
        ScheduleBuilder<T> tap(Consumer<ThreadContext<T>> handler);

        /**
         * Set delay before first execution.
         *
         * @param duration - wait timeout
         * @return ScheduleBuilder
         */
        @NotNull
        ScheduleBuilder<T> delay(@NotNull Duration duration);

        /**
         * Specify that need path requested authenticated user to background process thread
         *
         * @return this
         */
        @NotNull
        ScheduleBuilder<T> auth();

        // default false
        @NotNull
        ScheduleBuilder<T> hideOnUI(boolean value);

        // default true
        @NotNull
        ScheduleBuilder<T> hideOnUIAfterCancel(boolean value);

        // default true
        @NotNull
        ScheduleBuilder<T> cancelOnError(boolean value);

        // link log file to be able to read on UI
        @NotNull
        ScheduleBuilder<T> linkLogFile(@NotNull Path logFile);

        /**
         * Add value listener when run/schedule finished.
         *
         * @param name          - unique name of listener
         * @param valueListener - listener: First 2 parameters is value from run/schedule and previous value. Return boolean where is remove listener or not
         *                      after execute
         * @return this
         */
        @NotNull
        ScheduleBuilder<T> valueListener(@NotNull String name, @NotNull ThrowingBiFunction<T, T, Boolean, Exception> valueListener);
    }

    interface ThreadPuller {

        @NotNull
        ThreadPuller addThread(@NotNull String name, @Nullable String description, @NotNull Date creationTime,
                               @Nullable String state, @Nullable String errorMessage, @Nullable String bigDescription);

        @NotNull
        ThreadPuller addScheduler(@NotNull String name, @Nullable String description, @NotNull Date creationTime,
                                  @Nullable String state, @Nullable String errorMessage, @Nullable Duration period,
                                  int runCount,
                                  @Nullable String bigDescription);
    }

    interface ProcessContext {

        @NotNull
        String getName();

        boolean isStopped();

        void cancel(boolean sendSignal);
    }

    interface ThreadContext<T> {

        @NotNull
        String getName();

        void rename(@NotNull String newName);

        void writeStreamInfo(byte[] content);

        void attachInputStream(@NotNull InputStream inputStream, @NotNull InputStream errorStream);

        @NotNull
        String getState();

        void setState(@NotNull String state);

        @Nullable
        Object getMetadata(@NotNull String key);

        void setMetadata(@NotNull String key, @NotNull Object value);

        @Nullable
        String getDescription();

        void setDescription(@NotNull String description);

        boolean isStopped();

        default void cancel() {
            cancel(true);
        }

        void cancel(boolean mayInterruptIfRunning);

        /**
         * Reset thread. Set delay to initial state. If thread already finished - start it again. Start if not started yet
         */
        void reset();

        /**
         * Next schedule call as string
         *
         * @return next time to call
         */
        @Nullable
        String getTimeToNextSchedule();

        /**
         * Await at most timeout to finish process
         *
         * @param timeout - duration to wait
         * @return response returned by thread
         * @throws Exception -
         */
        @Nullable
        T await(@NotNull Duration timeout) throws Exception;

        /**
         * Add listener to fire when process finishes and value returned from process
         *
         * @param name          - unique listener name
         * @param valueListener - listener handler. Remove listener from next calls if value handler return true
         * @return if listener was added successfully or false if listener with same name already exists
         */
        boolean addValueListener(@NotNull String name, @NotNull ThrowingBiFunction<T, T, Boolean, Exception> valueListener);

        boolean removeValueListener(@NotNull String name);
    }
}
