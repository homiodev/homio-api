package org.touchhome.bundle.api.ui.field.condition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In edit mode some fields may be disabled for editing in some conditions.
 * i.e. Gpio pin in output mode can't have pull
 * resistance
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldDisableEditOnCondition {

    String value();
}
