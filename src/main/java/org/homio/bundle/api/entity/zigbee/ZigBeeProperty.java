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
     * This property name
     */
    String getKey();

    /**
     * Property human representation name
     */
    String getName(boolean shortFormat);

    String getDescription();

    String getIcon();

    String getIconColor();

    String getUnit();

    String getVariableID();

    /**
     * Must specify as ieeeAddress_key
     */
    String getEntityID();

    /**
     * Device's ieeeAddress
     */
    String getIeeeAddress();

    /**
     * Last readed value
     */
    State getLastValue();

    /**
     * Return duration since last event
     */
    Duration getTimeSinceLastEvent();

    /**
     * Is able to write value
     */
    boolean isWritable();

    /**
     * Is able to read value
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
