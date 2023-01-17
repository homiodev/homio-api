package org.touchhome.bundle.api.ui.field.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Disable create new tab(entity) */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldDisableCreateTab {}
