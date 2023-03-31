package org.homio.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.bundle.api.model.HasEntityIdentifier;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldEntityByClassListSelection.class)
public @interface UIFieldEntityByClassSelection {

    /**
     * Define base class/interface which should implement BaseEntity
     */
    Class<? extends HasEntityIdentifier> value();
}
