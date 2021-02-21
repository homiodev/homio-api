package org.touchhome.bundle.api.state;

import lombok.Getter;

public class StringType implements State {
    public static StringType EMPTY = new StringType("");

    @Getter
    private final String value;

    public StringType(String value) {
        this.value = value != null ? value : "";
    }

    @Override
    public String toString() {
        return value;
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
    public boolean boolValue() {
        return value.equals("1") || value.equalsIgnoreCase("true");
    }
}
