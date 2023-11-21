package org.homio.api.ui.field.selection.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.selection.dynamic.UIFieldDynamicSelection.UIFieldDynamicListSelection;
import org.jetbrains.annotations.NotNull;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(UIFieldDynamicListSelection.class)
public @interface UIFieldDynamicSelection {

    boolean rawInput() default false;

    // UI select order UIFieldDynamicSelection.icon or UIFieldSelectConfig.icon or 'fas fa-caret-down'
    String icon() default "";

    // override UIFieldSelectConfig color
    String iconColor() default "";

    /**
     * @return Target class for selection(for enums). see: ItemController.loadSelectOptions
     */
    @NotNull Class<? extends DynamicOptionLoader> value();

    /**
     * @return In case of same DynamicOptionLoader uses for few different fields, this parameter may distinguish business handling
     */
    String[] staticParameters() default {};

    /**
     * @return List of dependency fields that should be passed to DynamicOptionLoader from UI
     */
    String[] dependencyFields() default {};

    /**
     * Just for able to have many 'UIFieldDynamicSelection(...)'
     */
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface UIFieldDynamicListSelection {

        UIFieldDynamicSelection[] value();
    }
}
