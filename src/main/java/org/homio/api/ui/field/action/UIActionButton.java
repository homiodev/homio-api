package org.homio.api.ui.field.action;

import java.lang.annotation.*;
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
