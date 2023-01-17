package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// string with position matrix 3x3. i.e.: label position location, etc...
// Available values:
// 1x1 - Top Left
// 1x2 - Top Center
// 1x3 - Top Right
// 2x1 - Middle Left
// 2x2 - Middle Center
// 2x3 - Middle Right
// 3x1 - Bottom Left
// 3x2 - Bottom Center
// 3x3 - Bottom Right
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldPosition {

    /** Disable to select position at 2x2 */
    boolean disableCenter() default true;
}
