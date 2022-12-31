package org.touchhome.bundle.api.ui.field;

import org.touchhome.bundle.api.entity.BaseEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for able to link field to another page on UI with matched entityID
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldLinkToEntity {

    /**
     * Target class or base class with @UISidebarMenu annotation
     */
    Class<? extends BaseEntity> value();
}
