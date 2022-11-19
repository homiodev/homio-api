package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set<BaseEntity> to show list if entities in table with ability to CRUD in edit mode
 * This is like show all tabs but as table in General Tab
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEntity {

    /**
     * Background color
     */
    String bg();

    /**
     * Specify text on create new entity button
     */
    String addRow();

    /**
     * Specify condition to allow to create new entity
     */
    String addRowCondition() default "return true";

    /**
     * Specify condition to allow remove entity
     */
    String removeRowCondition() default "return true";

    String noContentTitle() default "NO_CONTENT";
}
