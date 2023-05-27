package org.homio.api.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UISidebarChildren {

    String icon();

    int order() default 1000;

    String color();

    boolean allowCreateItem() default true;
}
