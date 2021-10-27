package org.touchhome.bundle.api.state;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

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
    public String toString() {
        return value == null ? "" : value.toString();
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
    public boolean boolValue() {
        throw new IllegalStateException("Unable to fetch float value from string");
    }
}
