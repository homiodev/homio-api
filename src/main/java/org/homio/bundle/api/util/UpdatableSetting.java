package org.homio.bundle.api.util;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate field in bean to allow field value update it's depend on setting value
 * example:
 *
 * UpdatableSetting(FFMPEGInstallPathSetting.class) private UpdatableValue[Path] ffmpegLocation;
 */
@JacksonAnnotationsInside
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UpdatableSetting {
    Class<?> value();
}
