package org.touchhome.bundle.api.state;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.touchhome.common.util.CommonUtils;

import java.nio.charset.Charset;
import java.util.Map;

public abstract class State {

    public static State of(Object value) {
        if (value == null || value instanceof State) return (State) value;
        if (value instanceof Map) {
            return new JsonType(CommonUtils.OBJECT_MAPPER.convertValue(value, JsonNode.class));
        }
        if (Number.class.isAssignableFrom(value.getClass())) {
            if (value instanceof Double) {
                return new DecimalType((double) value);
            } else if (value instanceof Integer) {
                return new DecimalType((int) value);
            }
            return new DecimalType((long) value);
        }
        if (value instanceof Boolean) {
            return OnOffType.of((boolean) value);
        }
        if (value instanceof String) {
            return new StringType(value.toString());
        }
        return new ObjectType(value);
    }

    public boolean equalToOldValue() {
        throw new IllegalStateException("Unable to invoke equality for non state class");
    }

    public abstract float floatValue();

    public float floatValue(float defaultValue) {
        try {
            return floatValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public abstract int intValue();

    public abstract String stringValue();

    public long longValue() {
        return intValue();
    }

    public RawType toRawType() {
        return RawType.ofPlainText(stringValue());
    }

    public boolean boolValue() {
        String value = stringValue();
        return value.equals("1") || value.equalsIgnoreCase("true");
    }

    public byte[] byteArrayValue() {
        return toString().getBytes(Charset.defaultCharset());
    }

    @Override
    public String toString() {
        return stringValue();
    }

    public <T extends State> T as(Class<T> target) {
        if (target != null && target.isInstance(this)) {
            return target.cast(this);
        } else {
            return null;
        }
    }

    @SneakyThrows
    public State optional(String value) {
        return StringUtils.isEmpty(value) ? this :
                CommonUtils.findObjectConstructor(this.getClass(), String.class).newInstance(value);
    }
}
