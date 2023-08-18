package org.homio.api.ui.field.color;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldColorsMatch.class)
public @interface UIFieldColorMatch {
    /**
     * @return Apply 'color' field if string is mathes to 'value'
     */
    String value();

    String color();
}
