package org.touchhome.bundle.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Field with this annotation take ref field value as background color reference */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorBgRef {
    String value();

    boolean animate() default false;
}
