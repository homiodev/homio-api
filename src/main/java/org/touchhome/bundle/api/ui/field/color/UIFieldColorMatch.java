package org.touchhome.bundle.api.ui.field.color;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldColorsMatch.class)
public @interface UIFieldColorMatch {
    /** Apply 'color' field if string is matches to 'value' */
    String value();

    String color();
}
