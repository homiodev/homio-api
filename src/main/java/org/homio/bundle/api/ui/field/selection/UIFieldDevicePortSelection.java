package org.homio.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldDevicePortSelection {
    boolean allowInputRawText() default true;

    String icon() default "fas fa-shuffle";

    String iconColor() default "";
}
