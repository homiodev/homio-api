package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Define static options for SelectBox
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldStaticSelection {
    /**
     * @return List of options.
     * Example: {'1', '2', '3:SomeLabel', '5..8', '9..12;Channel %s'}
     */
    String[] value();

    /**
     * @return Specify SelectBox as text input(user may type text manually) with button to select from static selections
     */
    boolean allowInputRawText() default false;
}
