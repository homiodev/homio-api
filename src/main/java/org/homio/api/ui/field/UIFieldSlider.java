package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.Nullable;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSlider {

    double min() default Integer.MIN_VALUE;

    double max() default Integer.MAX_VALUE;

    double step() default 1D;

    @Nullable String header() default "";

    /**
     * @return fetch min value from minRef field instead of min if specified
     */
    @Nullable String minRef() default "";

    /**
     * @return fetch max value from maxRef field instead of ax if specified
     */
    @Nullable String maxRef() default "";

    /**
     * Specify extra number value that able to apply to slider i.e. for disable status
     *
     * @return extra value
     */
    String extraValue() default "";
}
