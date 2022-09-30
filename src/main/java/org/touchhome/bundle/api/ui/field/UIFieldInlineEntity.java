package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set<BaseEntity> to show list if entities in table with ability to CRUD in edit mode
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEntity {

    /**
     * Background color
     */
    String bg();

    /**
     * Specify text to add new entity.
     */
    String addRow();
}
