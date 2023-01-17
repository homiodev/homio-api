package org.touchhome.bundle.api.entity.widget.ability;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotate for method of classes which uses to select dataSource description */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SelectDataSourceDescription {}
