package org.homio.bundle.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set<BaseEntity> internal field to specify custom width in row in %. If not specified - 100 / num_of_fields
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEntityEditWidth {

    /**
     * Width in edit mode
     */
    int value();
}
