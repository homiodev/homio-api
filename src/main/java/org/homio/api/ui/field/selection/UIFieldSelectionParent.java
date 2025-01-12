package org.homio.api.ui.field.selection;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
   * @return Parent's name
   */
  @JsonIgnore
  String value();

  /**
   * @return Description. shows on ui at bottom-right place
   */
  @JsonIgnore
  String description() default "";

  /**
   * @return Specify parent's icon
   */
  @JsonIgnore
  String icon() default "";

  /**
   * @return Specify parent's icon color
   */
  @JsonIgnore
  String iconColor() default "";

  /**
   * In case if we want to configure parent dynamically. If entity/bean configured by anotation @UIFieldSelectionParent and implement SelectionParent
   * interface that it's merge both. interface SelectionParent overrides annotated values if values not null
   */
  interface SelectionParent {

    @JsonIgnore
    String getParentId();

    /**
     * @return Parent's name
     */
    @JsonIgnore
    String getParentName();

    /**
     * @return Description. shows on ui at bottom-right place
     */
    @JsonIgnore
    default String getParentDescription() {
      return "";
    }

    /**
     * @return Specify parent's icon
     */
    @JsonIgnore
    default String getParentIcon() {
      return "";
    }

    /**
     * @return Specify parent's icon color
     */
    @JsonIgnore
    default String getParentIconColor() {
      return "";
    }

    // if we want grouping of groups
    @JsonIgnore
    default SelectionParent getSuperParent() {
      return null;
    }
  }
}
