package org.homio.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Disable call by js engine */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JSDisableMethod {}
