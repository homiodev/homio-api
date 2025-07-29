package org.homio.api.ui.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
    order = 200,
    icon = "fas fa-database",
    color = "#8B2399",
    allowCreateItem = true,
    overridePath = UIRouteStorage.ROUTE)
public @interface UIRouteStorage {

  String ROUTE = "storage";

  String icon() default "";

  String color() default "";

  boolean allowCreateItem() default true;
}
