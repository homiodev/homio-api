package org.homio.api.ui.field.selection;

import org.homio.api.ContextVar.VariableType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldVariableSelection {

    boolean rawInput() default false;

    VariableType varType() default VariableType.Any;
}
