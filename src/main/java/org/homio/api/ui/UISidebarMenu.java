package org.homio.api.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UISidebarMenu {

    TopSidebarMenu parent() default TopSidebarMenu.ITEMS;

    String icon();

    String bg();

    boolean allowCreateNewItems() default false;

    int order() default 1000;

    /**
     * @return Available sorting fields. May contains 'name:icon:color' or 'name:icon' or 'name'
     */
    String[] sort() default "";

    /**
     * @return Path uses in ui as navigation link
     */
    String overridePath() default "";

    enum TopSidebarMenu {
        HARDWARE, ITEMS, MEDIA
    }
}
