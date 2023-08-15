package org.homio.api.model.endpoint;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.homio.api.EntityContext;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.Icon;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

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
     * Implement by property that has ability to write value. Calls external i.e. by scratch block
     *
     * @param state - value to set. May be need convert value to correct format
     */
    void writeValue(@NotNull State state);

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

    default @NotNull Set<String> getSelectValues() {
        throw new IllegalStateException("Must be implemented for 'select' type");
    }

    @NotNull EntityContext getEntityContext();

    @NotNull State getValue();

    /**
     * Uses to create action(s)(i.e. slider/switch/text) for endpoint row
     *
     * @return action builder
     */
    @Nullable UIInputBuilder createActionBuilder();

    /**
     * Uses to create settings for single endpoint
     *
     * @return settings builder
     */
    default @Nullable UIInputBuilder createSettingsBuilder() {
        return null;
    }

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


    @Getter
    @RequiredArgsConstructor
    enum EndpointType {
        bool((jsonObject, s) -> OnOffType.of(jsonObject.getBoolean(s))),
        number((jsonObject, s) -> new DecimalType(jsonObject.getInt(s))),
        dimmer((jsonObject, s) -> new DecimalType(jsonObject.getInt(s))),
        string((jsonObject, s) -> new StringType(jsonObject.getString(s))),
        select((jsonObject, s) -> new StringType(jsonObject.getString(s))),
        color((jsonObject, s) -> new StringType(jsonObject.getString(s)));

        private final BiFunction<JSONObject, String, State> reader;
    }
}
