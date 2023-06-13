package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify for entities to select entities by 'type'
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldEntityTypeSelection {

    String type();

    String[] staticParameters() default {};
}
