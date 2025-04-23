package org.homio.api.ui.field.color;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation on org.homio.api.model.Status field for showing color depend on value */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldColorStatusMatch {

  // Status.ONLINE
  String online() default "#1F8D2D";

  // Status.OFFLINE
  String offline() default "#969696";

  // Status.UNKNOWN
  String unknown() default "#818744";

  // Status.ERROR
  String error() default "#B22020";

  // Status.REQUIRE_AUTH
  String requireAuth() default "#8C3581";

  // Status.RUNNING
  String running() default "#B59324";

  // Status.WAITING
  String waiting() default "#506ABF";

  // Status.NOT_SUPPORTED
  String notSupported() default "#9C3E60";

  // Status.DONE
  String done() default "#399396";

  // Status.INITIALIZE
  String init() default "#CF79ED";

  // Status.CLOSING
  String closing() default "#992F5D";

  // Status.NOT_READY
  String notReady() default "#99A040";

  String restarting() default "#99A040";

  // handle color if message starts with 'error'/'requireAuth'/...
  boolean handlePrefixes() default false;
}
