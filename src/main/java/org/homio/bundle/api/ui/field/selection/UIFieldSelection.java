package org.homio.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.bundle.api.ui.action.DynamicOptionLoader;

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

    boolean lazyLoading() default false;

    String parentChildJoiner() default "/";

    /**
     * Interface to configure selection for IU
     */
    interface SelectionConfiguration {

        String selectionIcon();

        String selectionIconColor();
    }
}
