package org.touchhome.bundle.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on org.touchhome.bundle.api.model.Status field for showing color depend on value
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorStatusMatch {
    String online() default "#1F8D2D";

    String offline() default "#B22020";

    String unknown() default "#818744";

    String error() default "#9C4F4F";
}
