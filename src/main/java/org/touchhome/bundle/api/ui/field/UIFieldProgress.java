package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.touchhome.common.util.FlowMap;
import org.touchhome.common.util.Lang;

/** Progress bar. Must return int or UIFieldProgress.Progress Max value is 100! */
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
        private final String message;
        private final boolean showMessage;

        public Progress(int value, int maxValue, String message) {
            this(value, maxValue, message, false);
        }

        public Progress(int value, int maxValue, String message, boolean showMessage) {
            this((int) Math.ceil(value * 100f / maxValue), message, showMessage);
        }

        public Progress(int currentValue, int maxValue) {
            this(currentValue, maxValue, false);
        }

        public Progress(int currentValue, int maxValue, boolean showMessage) {
            this.value = (int) Math.ceil(currentValue * 100f / maxValue);
            this.showMessage = showMessage;
            this.message =
                    Lang.getServerMessage(
                            "USED_QUOTA",
                            FlowMap.of("PC", value, "VAL", currentValue, "MAX", maxValue));
        }
    }
}
