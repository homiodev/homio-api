package org.homio.api.ui.field;

import org.homio.api.entity.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for able to link field to another page on UI with matched entityID'
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldLinkToEntity {

  /**
   * @return Target class or base class with @UISidebarMenu annotation
   */
  Class<? extends BaseEntity> value();

  boolean applyTitle() default false;

  interface FieldLinkToEntityTitleProvider {
    String getLinkTitle();
  }

  record NavEntityTitle(String entityID, String title) {

  }
}
