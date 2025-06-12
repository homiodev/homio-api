package org.homio.api.ui.route;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UIRouteMenu(
        order = 200,
        icon = "fab fa-facebook-messenger",
        color = "#A16427",
        allowCreateItem = true,
        overridePath = UIRouteCommunication.ROUTE)
public @interface UIRouteCommunication {
    String ROUTE = "comm";

    String icon() default "";

    String color() default "";

    boolean allowCreateItem() default true;
}
