package org.homio.api;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines entry root for loading addon spring context
 */
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
