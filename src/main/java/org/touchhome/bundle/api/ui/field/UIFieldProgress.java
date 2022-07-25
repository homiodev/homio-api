package org.touchhome.bundle.api.ui.field;

import lombok.AllArgsConstructor;
import org.touchhome.common.util.FlowMap;
import org.touchhome.common.util.Lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Progress bar. Must return int or UIFieldProgress.Progress
 * Max value is 100!
 */
@Target({ElementType.METHOD})
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


    @AllArgsConstructor
    class Progress {
        public int value;
        public String message;

        public Progress(int value, int maxValue, String message) {
            this((int) Math.ceil(value * 100f / maxValue), message);
        }

        public Progress(int currentValue, int maxValue) {
            this.value = (int) Math.ceil(currentValue * 100f / maxValue);
            this.message = Lang.getServerMessage("USED_QUOTA", FlowMap.of(
                    "PC", value, "VAL", currentValue, "MAX", maxValue));
        }
    }
}
