package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for field/method with return type as 'String' to handle field as 'icon' type
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorPicker {
    /**
     * @return Allow user to use threshold functionality for showing different icon depend on 'value'
     */
    boolean allowThreshold() default false;

    /**
     * Add user ability to specify 'pulse' condition to UI. Color pulse is animation blink from black to specified color with 1-2sec timeout
     *
     * @return -
     */
    boolean pulseColorCondition() default false;

    /**
     * Set if need select 'source'(variables) value to change on threshold background
     *
     * @return -
     */
    boolean thresholdSource() default false;
}
