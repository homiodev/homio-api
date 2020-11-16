package org.touchhome.bundle.api.hquery.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CurlQuery {
    String value();

    String valueOnError() default "";

    int maxSecondsTimeout() default 60;

    boolean cache() default false;

    int cacheValid() default 0;

    boolean ignoreOnError() default false;

    Class<? extends Function<Object, Object>> mapping() default FallbackMapping.class;

    class FallbackMapping implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            return o;
        }
    }
}
