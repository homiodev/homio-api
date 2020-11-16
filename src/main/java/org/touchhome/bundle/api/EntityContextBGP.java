package org.touchhome.bundle.api;

import com.pivovarit.function.ThrowingRunnable;
import com.pivovarit.function.ThrowingSupplier;
import org.springframework.lang.Nullable;

import java.net.DatagramPacket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface EntityContextBGP {
    ThreadContext<Void> schedule(String name, int timeout, TimeUnit timeUnit, ThrowingRunnable<Exception> command, boolean showOnUI);

    default ThreadContext<Void> run(String name, ThrowingRunnable<Exception> command, boolean showOnUI) {
        return run(name, () -> {
            command.run();
            return null;
        }, showOnUI);
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
    }
}
