package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * List of common fields to configure select box
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSelectConfig {

    boolean addEmptySelection() default false;

    /**
     * @return icon for select picker
     */
    String icon() default "";

    /**
     * @return icon color for icon()
     */
    String iconColor() default "";

    // if set - add UI select box to select if no value specified
    String selectOnEmptyLabel() default "Select value";

    String selectOnEmptyIcon() default "fas fa-th-large";

    String selectOnEmptyColor() default "#A7D21E";

    /**
     * Annotation on selected field to show text when no values available from server
     * @return Text to show when no options available
     */
    String selectNoValue() default "";
}
