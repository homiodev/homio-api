package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.Nullable;

/**
 * Number field
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldNumber {

    int min() default Integer.MIN_VALUE;

    int max() default Integer.MAX_VALUE;

    /**
     * @return fetch min value from minRef field instead of min if specified
     */
    @Nullable String minRef() default "";

    /**
     * @return fetch max value from maxRef field instead of ax if specified
     */
    @Nullable String maxRef() default "";
}
