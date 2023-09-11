package org.homio.api.ui.field.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.api.ui.UIActionHandler;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIActionButtons.class)
public @interface UIActionButton {

    String name();

    String icon();

    String color() default "inherit";

    String style() default "";

    Class<? extends UIActionHandler> actionHandler();

    UIActionInput[] inputs() default {};
}
