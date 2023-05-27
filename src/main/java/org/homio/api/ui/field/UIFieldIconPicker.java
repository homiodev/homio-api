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
public @interface UIFieldIconPicker {
    /**
     * @return Allow user to select no icon to hide icon at all
     */
    boolean allowEmptyIcon() default false;

    /**
     * @return  Allow user to use threshold functionality for showing different icon depend on 'value'
     */
    boolean allowThreshold() default false;

    /**
     * @return  Add Size selector to icon picker. Works only if allowThreshold is true
     */
    boolean allowSize() default true;

    /**
     * @return  Add Spin selector to icon picker. Works only if allowThreshold is true
     */
    boolean allowSpin() default true;

    /**
     * @return Enable to select background shadow
     */
    boolean allowBackground() default false;
}
