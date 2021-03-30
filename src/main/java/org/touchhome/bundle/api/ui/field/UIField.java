package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIField {

    // show field in context menu
    boolean showInContextMenu() default false;

    // disable editing
    boolean readOnly() default false;

    boolean advanced() default false;

    /**
     * Should be available only in editMode. If true - readOnly flag ignored
     */
    boolean onlyEdit() default false;

    UIFieldType type() default UIFieldType.AutoDetect;

    int order();

    boolean hideOnEmpty() default false;

    // required not null validation before save
    boolean required() default false;

    // able to edit field directly from view mode (now works only in console)
    boolean inlineEdit() default false;

    boolean inlineEditWhenEmpty() default false;

    // override field name
    String label() default "";

    // override for field name, useful in methods
    String name() default "";

    // specify field color for ui
    String color() default "";

    // specify color for UI row
    String bg() default "";

    // if set - show content on full width
    boolean fullWidth() default false;

    // if set - hide field label on full width content
    boolean hideLabelInFullWidth() default true;
}
