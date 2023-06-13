package org.homio.api.ui.field;

import java.util.function.BiConsumer;
import org.homio.hquery.HQueryProgressBar;

public interface ProgressBar extends BiConsumer<Double, String> {

    void progress(double progress, String message);

    default void done() {
        progress(100, "Done");
    }

    @Override
    default void accept(Double progress, String message) {
        progress(progress, message);
    }

    default HQueryProgressBar asHQuery() {
        return new HQueryProgressBar(0, 0, 0) {
            @Override
            public void progress(double value, String message, boolean isError) {
                ProgressBar.this.progress(value, message);
            }
        };
    }

    default HQueryProgressBar asHQuery(double min, double max, double expectedTimeToExecute) {
        return new HQueryProgressBar(min, max, expectedTimeToExecute) {
            @Override
            public void progress(double value, String message, boolean isError) {
                ProgressBar.this.progress(value, message);
            }
        };
    }
}
