package org.homio.bundle.api.entity.zigbee;

import java.time.Duration;
import java.util.function.Consumer;
import org.homio.bundle.api.exception.ProhibitedExecution;
import org.homio.bundle.api.state.State;

/**
 * Specify zigbee single endpoint
 */
public interface ZigBeeProperty {
    /**
     * @return This property name
     */
    String getKey();

    /**
     * @return Property human representation name
     * @param shortFormat -
     */
    String getName(boolean shortFormat);

    String getDescription();

    String getIcon();

    String getIconColor();

    String getUnit();

    String getVariableID();

    /**
     * @return Must specify as ieeeAddress_key
     */
    String getEntityID();

    /**
     * @return Device's ieeeAddress
     */
    String getIeeeAddress();

    /**
     * @return Last read value
     */
    State getLastValue();

    /**
     * @return duration since last event
     */
    Duration getTimeSinceLastEvent();

    /**
     * @return is able to write value
     */
    boolean isWritable();

    /**
     * @return is able to read value
     */
    boolean isReadable();

    void addChangeListener(String id, Consumer<State> changeListener);

    void removeChangeListener(String id);

    PropertyType getPropertyType();

    /**
     * Implement by property that has ability to read value.
     */
    default void readValue() {
        if (isReadable()) {
            throw new IllegalStateException("Method must be implemented by readable property");
        }
        throw new ProhibitedExecution();
    }

    /**
     * Implement by property that has ability to write value.
     * @param state -
     */
    default void writeValue(State state) {
        if (isWritable()) {
            throw new IllegalStateException("Method must be implemented by writable property");
        }
        throw new ProhibitedExecution();
    }

    enum PropertyType {
        bool, number, string
    }
}
