package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingBiFunction;
import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingRunnable;
import com.pivovarit.function.ThrowingSupplier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public interface EntityContextBGP {
    EntityContext getEntityContext();

    ThreadContext<Void> schedule(String name, int timeout, TimeUnit timeUnit, ThrowingRunnable<Exception> command, boolean showOnUI);

    default ThreadContext<Void> run(String name, ThrowingRunnable<Exception> command, boolean showOnUI) {
        return run(name, () -> {
            command.run();
            return null;
        }, showOnUI);
    }

    void runOnceOnInternetUp(String name, ThrowingRunnable<Exception> command);

    default ThreadContext<Void> runWithProgress(String key, ThrowingConsumer<String, Exception> command) {
        return runWithProgress(key, command, null);
    }

    default ThreadContext<Void> runWithProgress(String key, ThrowingConsumer<String, Exception> command, Runnable finallyBlock) {
        return run(key, () -> {
            getEntityContext().ui().progress(key, 0, key);
            try {
                command.accept(key);
            } finally {
                getEntityContext().ui().progressDone(key);
                if (finallyBlock != null) {
                    finallyBlock.run();
                }
            }
            return null;
        }, true);
    }

    <T> ThreadContext<T> run(String name, ThrowingSupplier<T, Exception> command, boolean showOnUI);

    boolean isThreadExists(String name);

    void cancelThread(String name);

    interface ThreadContext<T> {
        String getName();

        String getState();

        void setState(String state);

        String getDescription();

        void setDescription(String description);

        boolean isStopped();

        void cancel();

        T await(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException;

        void onError(Consumer<Exception> errorListener);

        /**
         * Add value listener when run/schedule finished.
         *
         * @param name          - unique name of listener
         * @param valueListener - listener: First 2 parameters is value from run/schedule and previous value.
         *                      Return boolean where is remove listener or not after execute
         * @return true if listeners successfully added
         */
        boolean addValueListener(String name, ThrowingBiFunction<T, T, Boolean, Exception> valueListener);
    }
}
