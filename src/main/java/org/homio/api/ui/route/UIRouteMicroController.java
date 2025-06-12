package org.homio.api.ui.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
        order = 50,
        icon = "fas fa-microchip",
        parent = UIRouteMenu.TopSidebarMenu.HARDWARE,
        color = "#7482d0",
        allowCreateItem = true,
        overridePath = UIRouteMicroController.ROUTE)
public @interface UIRouteMicroController {
    String ROUTE = "controllers";

    String icon() default "";

    String color() default "";

    boolean allowCreateItem() default true;
}
