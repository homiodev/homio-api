package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Uses to grouping fields. Grouped fields has border */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldGroup {

  /**
   * @return Group name
   */
  @NotNull
  String value();

  /**
   * @return Specify custom group order, otherwise ordering by group name
   */
  int order() default 0;

  /**
   * @return Define border color
   */
  @Nullable
  String borderColor() default "";
}
