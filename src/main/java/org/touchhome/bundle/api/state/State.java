package org.touchhome.bundle.api.state;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.nio.charset.Charset;

public interface State {

    float floatValue();

    int intValue();

    default String toFullString() {
        return stringValue();
    }

    default long longValue() {
        return intValue();
    }

    default boolean boolValue() {
        String value = stringValue();
        return value.equals("1") || value.equalsIgnoreCase("true");
    }

    default byte[] byteArrayValue() {
        return toString().getBytes(Charset.defaultCharset());
    }

    default String stringValue() {
        return toString();
    }

    default <T extends State> T as(Class<T> target) {
        if (target != null && target.isInstance(this)) {
            return target.cast(this);
        } else {
            return null;
        }
    }

    @SneakyThrows
    default State optional(String value) {
        return StringUtils.isEmpty(value) ? this : TouchHomeUtils.findObjectConstructor(this.getClass(), String.class).newInstance(value);
    }
}
