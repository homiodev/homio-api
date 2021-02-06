package org.touchhome.bundle.api.ui.field.action;

import javax.validation.constraints.Pattern;
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

    Validator[] validators() default {};

    String description() default "";

    // for numbers or for text length
    int min() default Integer.MIN_VALUE;

    // for numbers or for text length
    int max() default Integer.MAX_VALUE;

    // pattern for text
    Pattern pattern() default @Pattern(regexp = ".*");

    enum Type {
        text, json, textarea, password, number
    }

    enum Validator {
        ip, password, email, required
    }
}
