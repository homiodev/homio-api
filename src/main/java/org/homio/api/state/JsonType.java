package org.homio.api.state;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MimeTypeUtils;

@Getter
@Log4j2
@RequiredArgsConstructor
public class JsonType implements State, Comparable<JsonType> {

  private final JsonNode jsonNode;

  @SneakyThrows
  public JsonType(Object value) {
    if (value instanceof String strValue) {
      this.jsonNode = OBJECT_MAPPER.readValue(strValue, JsonNode.class);
    } else {
      this.jsonNode = OBJECT_MAPPER.valueToTree(value);
    }
  }

  public JsonNode get(String fullPath) {
    JsonNode node = jsonNode;
    for (String path : fullPath.split("/")) {
      node = node.path(path);
    }
    return node;
  }

  public boolean set(String value, String fullPath) {
    return setByPath((node, key) -> node.put(key, value), fullPath.split("/"));
  }

  public boolean set(int value, String fullPath) {
    return setByPath((node, key) -> node.put(key, value), fullPath.split("/"));
  }

  public boolean set(float value, String fullPath) {
    return setByPath((node, key) -> node.put(key, value), fullPath.split("/"));
  }

  public boolean set(boolean value, String fullPath) {
    return setByPath((node, key) -> node.put(key, value), fullPath.split("/"));
  }

  public boolean set(State value, String fullPath) {
    return setByPath(value::setAsNode, fullPath.split("/"));
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
  public Object rawValue() {
    return jsonNode;
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
  public void setAsNode(ObjectNode node, String key) {
    node.set(key, jsonNode);
  }

  @Override
  public int compareTo(@NotNull JsonType o) {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    JsonType jsonType = (JsonType) o;

    return jsonNode.equals(jsonType.jsonNode);
  }

  @Override
  public int hashCode() {
    return jsonNode.hashCode();
  }
}
