package org.touchhome.bundle.api.ui.field;

import java.util.function.BiConsumer;

public interface ProgressBar extends BiConsumer<Double, String> {
    void progress(double progress, String message);

    default void done() {
        progress(100, null);
    }

    @Override
    default void accept(Double progress, String message) {
        progress(progress,message);
    }
}
