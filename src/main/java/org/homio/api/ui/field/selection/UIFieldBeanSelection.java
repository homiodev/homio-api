package org.homio.api.ui.field.selection;

import java.lang.annotation.*;
import org.homio.api.ui.field.selection.UIFieldBeanSelection.UIFieldListBeanSelection;

/** Selector for beans */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldListBeanSelection.class)
public @interface UIFieldBeanSelection {

  Class<?> value() default
      Object
          .class; // if value is Object.class then uses method return type or field type to
                  // evalueate

  /**
   * Interface that uses by beans that has to be exposes via @UIFieldBeanSelection(value =
   * XXX.class) Some of beans may be hidden from UI
   */
  interface BeanSelectionCondition {

    default boolean isBeanVisibleForSelection() {
      return true;
    }
  }

  @Target({ElementType.FIELD, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface UIFieldListBeanSelection {

    UIFieldBeanSelection[] value();
  }
}
