package org.homio.bundle.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Configuration;

/**
 * Defines entry root for loading bundle spring context
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Configuration
public @interface BundleConfiguration {

    BundleConfiguration.Env[] env() default {}; // defines env variables.

    @interface Env {
        String key();

        String value();
    }
}
