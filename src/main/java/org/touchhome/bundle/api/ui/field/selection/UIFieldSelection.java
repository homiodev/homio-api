package org.touchhome.bundle.api.ui.field.selection;

import org.touchhome.bundle.api.DynamicOptionLoader;

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
}
