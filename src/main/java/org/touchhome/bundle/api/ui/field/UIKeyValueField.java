package org.touchhome.bundle.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Key/Value type that stores into string: i.e.:
 *
 * @UIKeyValueField(maxSize = 5, keyType = UIFieldType.Float, valueType = UIFieldType.ColorPicker, defaultKey = "0",
 * defaultValue = "#FFFFFF")
 * public String getThreshold() {
 * return getJsonData("threshold", "{}");
 * }
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIKeyValueField {
    int maxSize() default Integer.MAX_VALUE;

    UIFieldType keyType();

    // show keys on UI
    boolean showKey();

    String defaultKey();

    String keyFormat() default "{0}";

    UIFieldType valueType();

    String defaultValue();

    String valueFormat() default "{0}";

    KeyValueType keyValueType() default KeyValueType.object;

    enum KeyValueType {
        array, object
    }
}
