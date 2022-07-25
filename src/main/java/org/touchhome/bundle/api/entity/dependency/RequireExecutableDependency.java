package org.touchhome.bundle.api.entity.dependency;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequireExecutableDependencies.class)
public @interface RequireExecutableDependency {
    Class<? extends DependencyExecutableInstaller> value();
}
