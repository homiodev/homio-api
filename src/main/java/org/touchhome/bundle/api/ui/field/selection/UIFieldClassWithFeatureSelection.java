package org.touchhome.bundle.api.ui.field.selection;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldClassWithFeatureSelection {

    Class<? extends HasEntityIdentifier> value();

    String[] basePackages() default "org.touchhome";
}
