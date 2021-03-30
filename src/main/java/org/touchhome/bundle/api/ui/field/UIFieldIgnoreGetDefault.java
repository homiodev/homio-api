package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method that need ignore to fetch default value. Uses in case when method implementation calls 3-part functions,
 * whereas default value fetched by reflection
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldIgnoreGetDefault {

}
