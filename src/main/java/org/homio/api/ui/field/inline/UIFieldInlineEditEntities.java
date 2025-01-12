package org.homio.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set[BaseEntity] to edit list in 'table' mode
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEditEntities {

  /**
   * @return Background color
   */
  String bg();

  /**
   * @return Specify text on create new entity button
   */
  String addRowLabel() default "ADD_ENTITY";

  /**
   * @return Specify condition to allow to create new entity
   */
  String addRowCondition() default "return true";

  /**
   * @return Specify condition to allow remove entity
   */
  String removeRowCondition() default "return true";

  String noContentTitle() default "W.ERROR.NO_CONTENT";
}
