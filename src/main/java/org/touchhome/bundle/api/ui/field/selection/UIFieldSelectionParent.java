package org.touchhome.bundle.api.ui.field.selection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation intended for grouping selection
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldSelectionParent {
    /**
     * Parent's name
     */
    String value();

    /**
     * Description. shows on ui at bottom-right place
     */
    String description() default "";

    /**
     * Specify parent's icon
     */
    String icon() default "";

    /**
     * Specify parent's icon color
     */
    String iconColor() default "";

    /**
     * In case if we want to configure parent dynamically. If entity/bean configured by anotation @UIFieldSelectionParent and
     * implement SelectionParent interface that it's merge both. interface SelectionParent overrides annotated values if values
     * not null
     */
    interface SelectionParent {
        /**
         * Parent's name
         */
        String getParentName();

        /**
         * Description. shows on ui at bottom-right place
         */
        default String getParentDescription() {
            return "";
        }

        /**
         * Specify parent's icon
         */
        default String getParentIcon() {
            return "";
        }

        /**
         * Specify parent's icon color
         */
        default String getParentIconColor() {
            return "";
        }
    }
}
