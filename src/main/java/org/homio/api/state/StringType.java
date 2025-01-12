package org.homio.api.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
public class StringType implements State {

  public static StringType EMPTY = new StringType("");

  private final @NotNull String value;

  @Getter
  @Setter
  private String oldValue;

  public StringType(String value) {
    this.value = value != null ? value : "";
  }

  public StringType(int value) {
    this.value = String.valueOf(value);
  }

  @Override
  public boolean equalToOldValue() {
    return Objects.equals(value, oldValue);
  }

  @Override
  public float floatValue() {
    return Float.parseFloat(value);
  }

  @Override
  public int intValue() {
    return Integer.parseInt(value);
  }

  @Override
  public Object rawValue() {
    return value;
  }

  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public boolean boolValue() {
    return value.equals("ON") || value.equals("1") || value.equalsIgnoreCase("true");
  }

  @Override
  public void setAsNode(ObjectNode node, String key) {
    node.put(key, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StringType that = (StringType) o;

    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return stringValue();
  }
}
