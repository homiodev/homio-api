package org.touchhome.bundle.api.ui;

import org.touchhome.bundle.api.ui.action.UIActionHandler;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UISidebarButtons.class)
public @interface UISidebarButton {

    String buttonIcon();

    String buttonIconColor();

    String buttonText() default "";

    String buttonTitle() default "";

    String confirm() default "";

    /**
     * Target class for handle button. May be a bean or POJO class
     */
    Class<? extends UIActionHandler> handlerClass();
}
