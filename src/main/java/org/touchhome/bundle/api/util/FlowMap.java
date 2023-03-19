package org.touchhome.bundle.api.util;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class FlowMap {
    @Getter
    private final Map<String, String> params = new HashMap<>();

    private FlowMap(String name, Object value, String name1, Object value1, String name2, Object value2, String name3,
                    Object value3) {
        this.params.put(name, String.valueOf(value));
        if (name1 != null) {
            this.params.put(name1, value1 == null ? "" : String.valueOf(value1));
        }
        if (name2 != null) {
            this.params.put(name2, value2 == null ? "" : String.valueOf(value2));
        }
        if (name3 != null) {
            this.params.put(name3, value3 == null ? "" : String.valueOf(value3));
        }
    }

    public static FlowMap of(String name, Object value) {
        return new FlowMap(name, value, null, null, null, null, null, null);
    }

    public static FlowMap of(String name, Object value, String name1, Object value1) {
        return new FlowMap(name, value, name1, value1, null, null, null, null);
    }

    public static FlowMap of(String name, Object value, String name1, Object value1, String name2, Object value2) {
        return new FlowMap(name, value, name1, value1, name2, value2, null, null);
    }

    public static FlowMap of(String name, Object value, String name1, Object value1, String name2, Object value2, String name3,
                             Object value3) {
        return new FlowMap(name, value, name1, value1, name2, value2, name3, value3);
    }
}
