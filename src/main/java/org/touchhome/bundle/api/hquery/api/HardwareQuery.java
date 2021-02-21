package org.touchhome.bundle.api.hquery.api;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(HardwareQueries.class)
public @interface HardwareQuery {
    String name();

    String[] value();

    int maxSecondsTimeout() default 60;

    // define directory from which should start process
    String dir() default "";

    boolean printOutput() default false;

    boolean ignoreOnError() default false;

    /**
     * Set this to true if you want parse error from commands. This value also set ignoreOnError as true
     */
    boolean redirectErrorsToInputs() default false;

    String[] win() default "";

    // how long cache valid in sec
    int cacheValid() default 0;

    String valueOnError() default "";
}
