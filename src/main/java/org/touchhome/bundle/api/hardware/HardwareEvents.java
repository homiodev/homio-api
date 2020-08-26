package org.touchhome.bundle.api.hardware;

import java.util.function.Consumer;

public interface HardwareEvents {

    void setListener(String key, Consumer<Object> listener);

    default void fireEvent(String key) {
        fireEvent(key, null);
    }

    default void addEvent(String key) {
        this.addEvent(key, key);
    }

    void fireEvent(String key, Object value);

    void addEvent(String key, String name);

    void addEventAndFire(String key, String name, Object value);

    default void addEventAndFire(String key, String name) {
        addEventAndFire(key, name, null);
    }
}
