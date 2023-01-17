package org.touchhome.bundle.api.ui.field.selection;

import java.lang.annotation.*;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldEntityByClassListSelection.class)
public @interface UIFieldEntityByClassSelection {

    /** Define base class/interface which should implement BaseEntity */
    Class<? extends HasEntityIdentifier> value();
}
