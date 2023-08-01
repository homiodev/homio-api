package org.homio.api.model.endpoint;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.homio.api.EntityContext;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.Icon;
import org.homio.api.state.State;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specify device single endpoint
 */
public interface DeviceEndpoint extends Comparable<DeviceEndpoint> {

    String ENDPOINT_DEVICE_STATUS = "device_status";
    String ENDPOINT_LAST_SEEN = "last_seen";
    String ENDPOINT_BATTERY = "battery";
    String ENDPOINT_SIGNAL = "linkquality";
    String ENDPOINT_TEMPERATURE = "temperature";
    String ENDPOINT_HUMIDITY = "humidity";

    @NotNull String getEndpointName();

    /**
     * @param shortFormat -
     * @return Property human representation name
     */
    @NotNull String getName(boolean shortFormat);

    @Nullable String getDescription();

    @NotNull Icon getIcon();

    @Nullable String getUnit();

    @Nullable String getVariableID();

    @NotNull String getDeviceID();

    @NotNull String getEndpointEntityID();

    default @NotNull String getEntityID() {
        return getDeviceID() + "_" + getEndpointEntityID();
    }

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

    int getOrder();

    long getUpdated();

    void addChangeListener(@NotNull String id, @NotNull Consumer<State> changeListener);

    void removeChangeListener(@NotNull String id);

    @NotNull DeviceEndpoint.EndpointType getEndpointType();

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

    /**
     * @return If visible on UI
     */
    default boolean isVisible() {
        return true;
    }

    @Override
    default int compareTo(@NotNull DeviceEndpoint o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }

    default @NotNull List<String> getSelectValues() {
        throw new IllegalStateException("Must be implemented for 'select' type");
    }

    EntityContext getEntityContext();

    State getValue();

    @NotNull UIInputBuilder createUIInputBuilder();

    /**
     * @param endpoints list
     * @return find latest updated endpoint
     */
    static @NotNull Date getLastUpdated(@NotNull Collection<? extends DeviceEndpoint> endpoints) {
        return new Date(endpoints
            .stream()
            .max(Comparator.comparingLong(DeviceEndpoint::getUpdated))
            .map(DeviceEndpoint::getUpdated).orElse(0L));
    }


    enum EndpointType {
        bool, number, string, select
    }
}
