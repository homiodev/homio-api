package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specify confirm dialog on UI when user want to change value on UI when inlineEdit is true
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldInlineEditConfirm {

    @NotNull String value();

    @Nullable String dialogColor() default "";

    /**
     * Specify conditional function to check if need to show confirm dialog
     *
     * @return function or null
     */
    @Nullable String showCondition() default "";
}
