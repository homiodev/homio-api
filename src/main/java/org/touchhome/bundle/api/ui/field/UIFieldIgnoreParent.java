package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation for method that need ignore parent method's annotation at all */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldIgnoreParent {}
