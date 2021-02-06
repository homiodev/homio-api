package org.touchhome.bundle.api.measure;

import lombok.Getter;

public class ObjectType implements State {

    @Getter
    private final Object value;

    public ObjectType(Object value) {
        this.value = value;
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
