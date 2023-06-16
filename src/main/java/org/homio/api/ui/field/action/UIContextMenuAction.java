package org.homio.api.ui.field.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Action to be available via UI context menu/regular menu
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIContextMenuAction {
    String value();

    String icon() default "";

    String iconColor() default "";

    String confirmMessage() default "";

    String confirmMessageDialogColor() default "";

    UIActionInput[] inputs() default {};
}
