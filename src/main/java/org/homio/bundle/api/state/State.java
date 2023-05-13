package org.homio.bundle.api.state;

import static org.homio.bundle.api.util.CommonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.Charset;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.util.CommonUtils;

public interface State {

    static State of(Object value) {
        if (value == null || value instanceof State) return (State) value;
        if (value instanceof Map) {
            return new JsonType(OBJECT_MAPPER.convertValue(value, JsonNode.class));
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
        } else if (value instanceof String) {
            try {
                return new JsonType(OBJECT_MAPPER.readValue((String) value, JsonNode.class));
            } catch (Exception ignore) {}
            return new StringType(value.toString());
        }
        if (value instanceof JsonNode) {
            return new JsonType((JsonNode) value);
        }
        return new ObjectType(value);
    }

    default boolean equalToOldValue() {
        throw new IllegalStateException("Unable to invoke equality for non state class");
    }

    float floatValue();

    default float floatValue(float defaultValue) {
        try {
            return floatValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    int intValue();

    Object rawValue();

    String stringValue();

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
