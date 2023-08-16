package org.homio.api.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

public class ObjectType implements State {

    @Getter
    private final Object value;

    @Getter
    @Setter
    private Object oldValue;

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
        throw new IllegalStateException("Unable to fetch float value from string");
    }

    @Override
    public int intValue() {
        throw new IllegalStateException("Unable to fetch float value from string");
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
        throw new IllegalStateException("Unable to fetch float value from string");
    }

    @Override
    public void setAsNode(ObjectNode node, String key) {
        node.put(key, stringValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectType that = (ObjectType) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
