package org.touchhome.bundle.api.ui.field.color;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldColorsMatch.class)
public @interface UIFieldColorMatch {
    String value();

    String color();
}
