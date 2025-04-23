package org.homio.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation on Boolean field for showing color depend on true/false value */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorBooleanMatch {

  String True() default "#1F8D2D";

  String False() default "#B22020";
}
