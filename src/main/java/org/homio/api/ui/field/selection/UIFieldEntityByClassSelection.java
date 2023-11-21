package org.homio.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.api.model.HasEntityIdentifier;
import org.homio.api.ui.field.selection.UIFieldEntityByClassSelection.UIFieldEntityByClassListSelection;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldEntityByClassListSelection.class)
public @interface UIFieldEntityByClassSelection {

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
