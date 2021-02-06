package org.touchhome.bundle.api.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class FlowMap {
    @Getter
    private Map<String, String> params = new HashMap<>();

    private FlowMap(String name, String value, String name1, String value1, String name2, String value2, String name3, String value3) {
        this.params.put(name, value);
        if (name1 != null) {
            this.params.put(name1, value1 == null ? "" : value1);
        }
        if (name2 != null) {
            this.params.put(name2, value2 == null ? "" : value2);
        }
        if (name3 != null) {
            this.params.put(name3, value3 == null ? "" : value3);
        }
    }

    public static FlowMap of(String name, String value) {
        return new FlowMap(name, value, null, null, null, null, null, null);
    }

    public static FlowMap of(String name, int value) {
        return new FlowMap(name, String.valueOf(value), null, null, null, null, null, null);
    }

    public static FlowMap of(String name, String value, String name1, String value1) {
        return new FlowMap(name, value, name1, value1, null, null, null, null);
    }

    public static FlowMap of(String name, int value, String name1, int value1) {
        return new FlowMap(name, String.valueOf(value), name1, String.valueOf(value1), null, null, null, null);
    }

    public static FlowMap of(String name, String value, String name1, String value1, String name2, String value2) {
        return new FlowMap(name, value, name1, value1, name2, value2, null, null);
    }

    public static FlowMap of(String name, String value, String name1, String value1, String name2, String value2, String name3, String value3) {
        return new FlowMap(name, value, name1, value1, name2, value2, name3, value3);
    }
}
