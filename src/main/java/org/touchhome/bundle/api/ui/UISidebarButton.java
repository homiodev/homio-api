package org.touchhome.bundle.api.ui;

import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.condition.TrueCondition;
import org.touchhome.bundle.api.ui.action.UIActionHandler;

import java.lang.annotation.*;
import java.util.function.Predicate;

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
     * Target class for handle button
     */
    Class<? extends UIActionHandler> handlerClass();

    Class<? extends Predicate<EntityContext>> conditionalClass() default TrueCondition.class;
}
