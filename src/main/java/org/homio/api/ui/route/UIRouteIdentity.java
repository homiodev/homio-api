package org.homio.api.ui.route;

import org.homio.api.ui.route.UIRouteMenu.TopSidebarMenu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Common class for entities which respond for users/ssh/tls/etc... */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
    order = 300,
    icon = "fas fa-address-card",
    parent = TopSidebarMenu.ITEMS,
    color = "#9BA127",
    overridePath = UIRouteIdentity.ROUTE,
    allowCreateItem = true)
public @interface UIRouteIdentity {
  String ROUTE = "identity";

  String icon() default "";

  String color() default "";

  boolean allowCreateItem() default true;
}
