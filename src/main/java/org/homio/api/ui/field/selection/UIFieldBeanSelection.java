package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldBeanListSelection.class)
public @interface UIFieldBeanSelection {

    boolean lazyLoading() default false;

    Class<?> value() default Object.class; // if value is Object.class then uses method return type or field type to evalueate
}
