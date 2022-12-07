package org.touchhome.bundle.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set<BaseEntity> internal field to specify @UIField colspan
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEntityColSpan {

    /**
     * Table colspan.
     */
    int value();
}
