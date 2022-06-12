package org.touchhome.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * File selection
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldFileSelection {
    boolean allowInputRawText() default true;

    /**
     * If set - uses only local file system, otherwise uses all possible file systems
     */
    String rootPath() default "";

    boolean allowMultiSelect() default false;

    boolean showAllFileSystems() default true;

    boolean allowSelectDirs() default false;

    boolean allowSelectFiles() default true;

    String pattern() default ".*";

    String icon() default "fas fa-folder-open";

    String iconColor() default "";
}
