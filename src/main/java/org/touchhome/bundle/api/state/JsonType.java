package org.touchhome.bundle.api.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MimeTypeUtils;
import org.touchhome.bundle.api.util.TouchHomeUtils;

import java.util.function.BiConsumer;

@Log4j2
@RequiredArgsConstructor
public class JsonType implements State, Comparable<JsonType> {

    @Getter
    private final JsonNode jsonNode;

    @SneakyThrows
    public JsonType(String value) {
        this.jsonNode = TouchHomeUtils.OBJECT_MAPPER.readValue(value, JsonNode.class);
    }

    public JsonNode get(String... paths) {
        JsonNode node = jsonNode;
        for (String path : paths) {
            node = node.path(path);
        }
        return node;
    }

    public boolean set(String value, String... path) {
        return setByPath((node, key) -> node.put(key, value), path);
    }

    public boolean set(int value, String... path) {
        return setByPath((node, key) -> node.put(key, value), path);
    }

    public boolean set(float value, String... path) {
        return setByPath((node, key) -> node.put(key, value), path);
    }

    public boolean set(boolean value, String... path) {
        return setByPath((node, key) -> node.put(key, value), path);
    }

    public boolean setByPath(BiConsumer<ObjectNode, String> handler, String... path) {
        JsonNode node = jsonNode;
        for (int i = 0; i < path.length - 1; i++) {
            node = node.path(path[i]);
        }
        if (node instanceof ObjectNode) {
            handler.accept((ObjectNode) node, path[path.length - 1]);
            return true;
        }
        return false;
    }

    @Override
    public float floatValue() {
        return (float) jsonNode.asDouble();
    }

    @Override
    public int intValue() {
        return jsonNode.asInt();
    }

    @Override
    public String stringValue() {
        return jsonNode.toString();
    }

    @Override
    public String toString() {
        return stringValue();
    }

    @Override
    public RawType toRawType() {
        return new RawType(byteArrayValue(), MimeTypeUtils.APPLICATION_JSON_VALUE);
    }

    @Override
    public int compareTo(@NotNull JsonType o) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JsonType jsonType = (JsonType) o;

        return jsonNode.equals(jsonType.jsonNode);
    }

    @Override
    public int hashCode() {
        return jsonNode.hashCode();
    }
}
