package org.touchhome.bundle.api;

import java.lang.annotation.*;
import org.springframework.context.annotation.Configuration;

/** Defines entry root for loading bundle spring context */
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
