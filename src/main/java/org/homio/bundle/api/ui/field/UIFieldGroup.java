package org.homio.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Uses to grouping fields. Grouped fields has border
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldGroup {
    /**
     * Group name
     */
    String value();

    /**
     * Specify custom group order, otherwise ordering by group name
     */
    int order() default 0;

    /**
     * Define border color
     */
    String borderColor() default "";
}
