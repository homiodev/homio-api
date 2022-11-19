package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSlider {
    double min() default Integer.MIN_VALUE;

    double max() default Integer.MAX_VALUE;

    double step() default 1D;

    String header() default "";
}
