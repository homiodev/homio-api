package org.touchhome.bundle.api.hardware;

import org.touchhome.bundle.api.manager.En;
import org.touchhome.bundle.api.util.FlowMap;

import java.util.function.Consumer;

public interface HardwareEvents {

    void removeEvents(String... keys);

    void setListener(String key, Consumer<Object> listener);

    default void fireEvent(String key) {
        fireEvent(key, null);
    }

    default String addEvent(String key) {
        return this.addEvent(key, key);
    }

    void fireEvent(String key, Object value);

    // return key
    String addEvent(String key, String name);

    default String addEvent(String key, String name, FlowMap nameParams) {
        return addEvent(key, En.getServerMessage(name, nameParams));
    }

    String addEventAndFire(String key, String name, Object value);

    default String addEventAndFire(String key, String name, FlowMap nameParams, Object value) {
        return addEventAndFire(key, En.getServerMessage(name, nameParams), value);
    }

    default String addEventAndFire(String key, String name) {
        return addEventAndFire(key, name, null);
    }
}
