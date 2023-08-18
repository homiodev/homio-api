package org.homio.api.ui.field.selection;

import org.homio.api.model.HasEntityIdentifier;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldEntityByClassListSelection.class)
public @interface UIFieldEntityByClassSelection {

    /**
     * @return Define base class/interface which should implement BaseEntity
     */
    Class<? extends HasEntityIdentifier> value();

    String[] staticParameters() default {};
}
