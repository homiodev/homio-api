package org.touchhome.bundle.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotate Set<BaseEntity> to edit list in 'table' mode */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEditEntities {

    /** Background color */
    String bg();

    /** Specify text on create new entity button */
    String addRowLabel() default "ADD_ENTITY";

    /** Specify condition to allow to create new entity */
    String addRowCondition() default "return true";

    /** Specify condition to allow remove entity */
    String removeRowCondition() default "return true";

    String noContentTitle() default "NO_CONTENT";
}
