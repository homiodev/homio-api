package org.homio.bundle.api.ui.field;

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
     * Allow user to use threshold functionality for showing different icon depend on 'value'
     */
    boolean allowThreshold() default false;

    /**
     * Add user ability to specify 'animation' condition to UI.
     * Color animation is animation blink from black to specified color with 1-2sec timeout
     */
    boolean animateColorCondition() default false;
}
