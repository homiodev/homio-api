package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.api.ContextVar.VariableType;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldVariableSelection {

  boolean rawInput() default false;

  boolean requireWritable() default true;

  VariableType varType() default VariableType.Any;
}
