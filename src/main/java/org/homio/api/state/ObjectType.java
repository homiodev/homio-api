package org.homio.api.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ObjectType implements State {

  private final Object value;

  @Getter @Setter private Object oldValue;

  public ObjectType(Object value) {
    this.value = value;
  }

  public ObjectType(Object value, Object oldValue) {
    this.value = value;
    this.oldValue = oldValue;
  }

  @Override
  public boolean equalToOldValue() {
    return Objects.equals(value, oldValue);
  }

  @Override
  public float floatValue() {
    if (value instanceof Number num) {
      return num.floatValue();
    }
    throw new IllegalStateException("Unable to fetch float value from " + value.getClass());
  }

  @Override
  public int intValue() {
    if (value instanceof Number num) {
      return num.intValue();
    }
    throw new IllegalStateException("Unable to fetch int value from " + value.getClass());
  }

  @Override
  public Object rawValue() {
    return value;
  }

  @Override
  public String stringValue() {
    return value == null ? "" : value.toString();
  }

  @Override
  public String toString() {
    return stringValue();
  }

  @Override
  public boolean boolValue() {
    if (value instanceof Boolean bool) {
      return bool;
    }
    throw new IllegalStateException("Unable to fetch boolean value from " + value.getClass());
  }

  @Override
  public void setAsNode(ObjectNode node, String key) {
    node.put(key, stringValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ObjectType that = (ObjectType) o;

    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return value != null ? value.hashCode() : 0;
  }
}
