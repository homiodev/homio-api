package org.homio.api.state;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;

public interface State {

    State empty = new State() {
        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public Object rawValue() {
            return "";
        }

        @Override
        public String stringValue() {
            return "";
        }

        @Override
        public void setAsNode(ObjectNode node, String key) {

        }

        @Override
        public String toString() {
            return "N/A";
        }
    };

    static @NotNull State of(Object value) {
        if (value == null) {
            return empty;
        }
        if (value instanceof State) {
            return (State) value;
        }
        if (value instanceof Map) {
            return new JsonType(OBJECT_MAPPER.convertValue(value, JsonNode.class));
        }
        if (Number.class.isAssignableFrom(value.getClass())) {
            return switch (value) {
                case Float v -> new DecimalType((float) value);
                case Double v -> new DecimalType((double) value);
                case Integer i -> new DecimalType((int) value);
                default -> new DecimalType((long) value);
            };
        }
        switch (value) {
            case Boolean ignored -> {
                return OnOffType.of((boolean) value);
            }
            case String str -> {
                if (str.startsWith("{") || str.startsWith("[")) {
                    try {
                        return new JsonType(OBJECT_MAPPER.readValue((String) value, JsonNode.class));
                    } catch (Exception ignore) {
                    }
                }
                return new StringType(value.toString());
            }
            case JsonNode jsonNode -> {
                return new JsonType(jsonNode);
            }
            default -> {
            }
        }
        return new ObjectType(value);
    }

    default boolean equalToOldValue() {
        throw new IllegalStateException("Unable to invoke equality for non state class");
    }

    float floatValue();

    default double doubleValue() {
        return floatValue();
    }

    default double doubleValue(double defaultValue) {
        return floatValue((float) defaultValue);
    }

    default float floatValue(float defaultValue) {
        try {
            return floatValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    int intValue();

    default int intValue(int defaultValue) {
        try {
            return intValue();
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    Object rawValue();

    String stringValue();

    default String stringValue(String defValue) {
        return StringUtils.defaultIfEmpty(stringValue(), defValue);
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

    default boolean boolValue(float trueThreshold) {
        if (this instanceof DecimalType dt) {
            return dt.floatValue() >= trueThreshold;
        }
        String value = stringValue();
        return value.equals("1") || value.equalsIgnoreCase("true");
    }

    default String boolValue(String onValue, String offValue) {
        return boolValue() ? onValue : offValue;
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
        return StringUtils.isEmpty(value)
                ? this
                : Objects.requireNonNull(CommonUtils.findObjectConstructor(this.getClass(), String.class))
                .newInstance(value);
    }

    void setAsNode(ObjectNode node, String key);
}
