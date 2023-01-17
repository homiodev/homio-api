package org.touchhome.bundle.api.ui.field.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Upload action to be available via UI context menu/regular menu */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIContextMenuUploadAction {
    String value();

    String icon() default "";

    String iconColor() default "";

    String[] supportedFormats();
}
