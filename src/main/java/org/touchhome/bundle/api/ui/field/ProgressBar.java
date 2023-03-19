package org.touchhome.bundle.api.ui.field;

public interface ProgressBar {
    void progress(double progress, String message);

    default void done() {
        progress(100, null);
    }
}
