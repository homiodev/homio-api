package org.touchhome.bundle.api.ui.field;

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
     * Allow user to select no icon to hide icon at all
     */
    boolean allowEmptyIcon() default false;

    /**
     * Allow user to use threshold functionality for showing different icon depend on 'value'
     */
    boolean allowThreshold() default false;
}
