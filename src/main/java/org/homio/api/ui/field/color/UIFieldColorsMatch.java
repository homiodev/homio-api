package org.homio.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Aggregator for UIFieldColorSource type. Do not use explicitly */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorsMatch {

  UIFieldColorMatch[] value();
}
