package org.homio.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldColorsMatch.class)
public @interface UIFieldColorMatch {

  /**
   * @return Apply 'color' field if string is mathes to 'value'
   */
  String value();

  String color();
}
