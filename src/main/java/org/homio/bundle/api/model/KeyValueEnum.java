package org.homio.bundle.api.model;

public interface KeyValueEnum {
    default String getKey() {
        return ((Enum) this).name();
    }

    default String getValue() {
        return this.toString();
    }
}
