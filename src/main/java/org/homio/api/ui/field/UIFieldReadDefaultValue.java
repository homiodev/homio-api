package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation mark methods/fields to read default values to be able to revert it.
 * Reading default value possible in case if method/field annotated with @UIFieldReadDefaultValue and @UIField(disableEdit=false)
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldReadDefaultValue {
}
