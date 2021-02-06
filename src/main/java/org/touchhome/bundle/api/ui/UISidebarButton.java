package org.touchhome.bundle.api.ui;

import org.touchhome.bundle.api.ui.action.UIActionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UISidebarButton {

    String buttonIcon();

    String buttonIconColor();

    String buttonText() default "";

    String buttonTitle() default "";

    String confirm() default "";

    /**
     * Target class for handle button
     */
    Class<? extends UIActionHandler> handlerClass();
}
