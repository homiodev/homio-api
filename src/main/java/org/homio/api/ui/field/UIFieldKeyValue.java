package org.homio.api.ui.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Key/Value type that stores into string: i.e.:
 * <p>
 * UIKeyValueField(maxSize = 5, keyType = UIFieldType.Float, valueType = UIFieldType.ColorPicker, defaultKey = "0",
 * defaultValue = "#FFFFFF")
 * public String getThreshold() {
 * return getJsonData("threshold", "{}");
 * }
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UIFieldKeyValue {

    int maxSize() default Integer.MAX_VALUE;

    Option[] options() default {};

    UIFieldType keyType() default UIFieldType.String;

    // show keys on UI
    boolean showKey() default true;

    String defaultKey() default "";

    String keyPlaceholder() default "";

    String keyFormat() default "{0}";

    UIFieldType valueType() default UIFieldType.String;

    String defaultValue() default "";

    String valuePlaceholder() default "";

    String valueFormat() default "{0}";

    KeyValueType keyValueType() default KeyValueType.object;

    enum KeyValueType {
        array, object
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Option {

        String key();

        String icon() default "";

        String[] values();
    }
}
