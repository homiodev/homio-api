package org.homio.api.ui;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UISidebarChildren {

  @NotNull String icon();

  int order() default 1000;

  String color();

  boolean allowCreateItem() default true;

  int maxAllowCreateItem() default -1;
}
