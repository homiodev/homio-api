package org.homio.api.ui.field.action;

import jakarta.validation.constraints.Pattern;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIActionInput {

    String name();

    String value() default "";

    Type type() default Type.text;

    String description() default "";

    boolean required() default false;

    // for numbers or for text length
    int min() default Integer.MIN_VALUE;

    // for numbers or for text length
    int max() default Integer.MAX_VALUE;

    // for select box. i.e. {"1", "2..12;Value %s", "14:Fourteen"}
    String[] values() default "";

    // pattern for text
    Pattern pattern() default @Pattern(regexp = ".*");

    enum Type {
        text, json, textarea, password, number, info, bool, ip, select
    }
}
