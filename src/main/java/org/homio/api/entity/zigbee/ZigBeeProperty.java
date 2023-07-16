package org.homio.api.entity.zigbee;

import java.time.Duration;
import java.util.function.Consumer;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.Icon;
import org.homio.api.state.State;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specify zigbee single endpoint
 */
public interface ZigBeeProperty {

    /**
     * @return This property name
     */
    @NotNull String getKey();

    /**
     * @param shortFormat -
     * @return Property human representation name
     */
    @NotNull String getName(boolean shortFormat);

    @Nullable String getDescription();

    @NotNull Icon getIcon();

    @Nullable String getUnit();

    @NotNull String getVariableID();

    /**
     * @return Must specify as ieeeAddress_key
     */
    @NotNull String getEntityID();

    /**
     * @return Device's ieeeAddress
     */
    @NotNull String getIeeeAddress();

    /**
     * @return Last read value
     */
    @NotNull State getLastValue();

    /**
     * @return duration since last event
     */
    @NotNull Duration getTimeSinceLastEvent();

    /**
     * @return is able to write value
     */
    boolean isWritable();

    /**
     * @return is able to read value
     */
    boolean isReadable();

    void addChangeListener(@Nullable String id, @Nullable Consumer<State> changeListener);

    void removeChangeListener(@Nullable String id);

    @NotNull PropertyType getPropertyType();

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
     *
     * @param state -
     */
    default void writeValue(@NotNull State state) {
        if (isWritable()) {
            throw new IllegalStateException("Method must be implemented by writable property");
        }
        throw new ProhibitedExecution();
    }

    enum PropertyType {
        bool, number, string
    }
}
