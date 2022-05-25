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

    // if not specified - CommonUtils.getRootPath() will be used
    String rootPath() default "";

    boolean allowSelectDirs() default false;

    boolean allowSelectFiles() default true;

    boolean flatStructure() default false;

    boolean skipRootInTreeStructure() default true;

    String[] extensions() default {};

    String icon() default "fas fa-folder-open";

    String iconColor() default "";

    int levels() default 1;
}
