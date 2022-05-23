package org.touchhome.bundle.api.ui.field.selection;

import org.touchhome.bundle.api.ui.action.DynamicOptionLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSelection {
    /**
     * Target class for selection(for enums). see: ItemController.loadSelectOptions
     */
    Class<? extends DynamicOptionLoader> value();

    /**
     * In case of same DynamicOptionLoader uses for few different fields, this parameter may distinguish business handling
     *
     * @return
     */
    String[] staticParameters() default {};

    /**
     * Set ui as textInout with select button if 'allowInputRawText' is true, and pure select box if false
     */
    boolean allowInputRawText() default false;

    /**
     * List of dependency fields that should be passed to DynamicOptionLoader from UI
     */
    String[] dependencyFields() default {};
}
