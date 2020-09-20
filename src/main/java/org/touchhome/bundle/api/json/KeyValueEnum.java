package org.touchhome.bundle.api.json;

public interface KeyValueEnum {
    default String getKey() {
        return ((Enum) this).name();
    }

    default String getValue() {
        return this.toString();
    }
}
