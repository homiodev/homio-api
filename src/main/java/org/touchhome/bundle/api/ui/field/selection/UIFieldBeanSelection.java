package org.touchhome.bundle.api.ui.field.selection;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldBeanListSelection.class)
public @interface UIFieldBeanSelection {
    boolean lazyLoading() default false;

    Class<?> value() default Object.class; // if value is Object.class then uses method return type or field type to evalueate
}
