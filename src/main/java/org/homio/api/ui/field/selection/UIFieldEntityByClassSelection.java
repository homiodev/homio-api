package org.homio.api.ui.field.selection;

import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.ui.field.selection.UIFieldEntityByClassSelection.UIFieldEntityByClassListSelection;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldEntityByClassListSelection.class)
public @interface UIFieldEntityByClassSelection {

    boolean rawInput() default false;

    /**
     * @return Define base class/interface which should implement BaseEntity
     */
    Class<? extends HasEntityIdentifier> value();

    String[] staticParameters() default {};

    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface UIFieldEntityByClassListSelection {

        UIFieldEntityByClassSelection[] value();
    }
}
