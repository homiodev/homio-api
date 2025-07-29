package org.homio.api.ui.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
    order = 900,
    icon = "fas fa-puzzle-piece",
    color = "#939E18",
    allowCreateItem = true,
    overridePath = UIRouteMisc.ROUTE)
public @interface UIRouteMisc {
  String ROUTE = "misc";

  String icon() default "";

  String color() default "";

  boolean allowCreateItem() default true;
}
