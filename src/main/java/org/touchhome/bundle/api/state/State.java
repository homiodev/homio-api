package org.touchhome.bundle.api.state;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.common.util.CommonUtils;

import java.nio.charset.Charset;

public interface State {

    default boolean equalToOldValue() {
        throw new IllegalStateException("Unable to invoke equality for non state class");
    }

    float floatValue();

    int intValue();

    default String toFullString() {
        return stringValue();
    }

    default long longValue() {
        return intValue();
    }

    default RawType toRawType() {
        return RawType.ofPlainText(stringValue());
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
        return StringUtils.isEmpty(value) ? this :
                CommonUtils.findObjectConstructor(this.getClass(), String.class).newInstance(value);
    }
}
