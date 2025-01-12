package org.homio.api.ui.field.inline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate to allow to show field with full width inside table and ignore all rest fields
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineGroup {

  /**
   * @return Condition when apply field
   */
  String value();

  // If able to edit group
  boolean editable() default false;
}
