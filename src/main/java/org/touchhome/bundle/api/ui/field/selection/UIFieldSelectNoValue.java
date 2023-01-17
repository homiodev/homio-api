package org.touchhome.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation on selected field to show text when no values available from server */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSelectNoValue {

    /** Text to show when no options available */
    String value();
}
