package org.homio.api;

import java.lang.annotation.*;
import org.springframework.context.annotation.Configuration;

/** Defines entry root for loading addon spring context */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Configuration
public @interface AddonConfiguration {

  AddonConfiguration.Env[] env() default {}; // defines env variables.

  @interface Env {

    String key();

    String value();
  }
}
