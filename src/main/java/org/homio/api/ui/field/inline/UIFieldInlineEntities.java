package org.homio.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate Set[BaseEntity] to show list if entities in table with ability to CRUD in edit mode This
 * is like show all tabs but as table in General Tab
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEntities {

  /**
   * @return Background color
   */
  String bg();

  String noContentTitle() default "W.ERROR.NO_CONTENT";

  /**
   * Interface that has to be implemented by UIFieldInlineEntities List(... extends InlineEntity)
   * items
   */
  interface InlineEntity {
    String getEntityID();
  }
}
