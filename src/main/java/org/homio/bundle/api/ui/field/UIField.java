package org.homio.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIField {

    /**
     * Show field in context menu
     */
    boolean showInContextMenu() default false;

    /**
     * Hide field from edit mode
     */
    boolean hideInEdit() default false;

    /**
     * Hide field in view mode
     */
    boolean hideInView() default false;

    /**
     * Disable editing but show in view/edit mode
     */
    boolean disableEdit() default false;

    UIFieldType type() default UIFieldType.AutoDetect;

    int order();

    boolean hideOnEmpty() default false;

    // required not null validation before save
    boolean required() default false;

    // able to edit field directly from view mode (now works only in console)
    boolean inlineEdit() default false;

    boolean copyButton() default false;

    /**
     * Show revert button to set initial value
     */
    boolean isRevert() default false;

    boolean inlineEditWhenEmpty() default false;

    // override field name
    String label() default "";

    // override for field name, useful in methods
    String name() default "";

    // specify field color for ui
    String color() default "";

    // for different purposes
    String icon() default "";

    // specify color for UI row
    String bg() default "";

    // if set - show content on full width
    boolean fullWidth() default false;

    // if set - hide field label on full width content
    boolean hideLabelInFullWidth() default true;

    String style() default "";
}
