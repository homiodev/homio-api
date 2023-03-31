package org.homio.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.bundle.api.util.FlowMap;
import org.homio.bundle.api.util.Lang;

/**
 * Progress bar. Must return int or UIFieldProgress.Progress
 * Max value is 100!
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldProgress {
    String color() default "";

    String fillColor() default "";

    UIFieldProgressColorChange[] colorChange() default {};

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface UIFieldProgressColorChange {
        String color();

        int whenMoreThan();
    }

    @Getter
    @RequiredArgsConstructor
    class Progress {
        private final int value;
        private final int max;
        private final String message;
        private final boolean showMessage;

        public static Progress of(int value, int maxValue, String message) {
            return Progress.of(value, maxValue, message, false);
        }

        public static Progress of(int value, int maxValue, String message, boolean showMessage) {
            return new Progress((int) Math.ceil(value * 100f / maxValue), maxValue, message, showMessage);
        }

        public static Progress of(int value, int maxValue) {
            return Progress.of(value, maxValue, false);
        }

        public static Progress of(int value, int maxValue, boolean showMessage) {
            return Progress.of(value, maxValue, Lang.getServerMessage("USED_QUOTA", FlowMap.of(
                    "PC", value, "VAL", value, "MAX", maxValue)), showMessage);
        }
    }
}
