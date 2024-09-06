package org.homio.api.entity;

import java.lang.annotation.*;

/**
 * System creates exact single entity if not exists yet
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CreateSingleEntity {
    String name() default "";

    boolean disableDelete() default true;
}