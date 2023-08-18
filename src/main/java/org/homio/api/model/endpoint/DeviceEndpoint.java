package org.homio.api.model.endpoint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.EntityContext;
import org.homio.api.exception.ProhibitedExecution;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.item.UIInfoItemBuilder.InfoType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    default @NotNull Float getMin() {
        throw new IllegalStateException("Must be implemented for 'slider' type");
    }

    default @NotNull Float getMax() {
        throw new IllegalStateException("Must be implemented for 'slider' type");
    }

    @NotNull EntityContext getEntityContext();

    @NotNull State getValue();

    void setValue(State value, boolean externalUpdate);

    /**
     * Uses to create action(s)(i.e. slider/switch/text) for endpoint row
     *
     * @return action builder
     */
    default @Nullable UIInputBuilder createActionBuilder() {
        UIInputBuilder uiInputBuilder = getEntityContext().ui().inputBuilder();
        State value = getValue();
        UIInputBuilder actionBuilder = null;

        if (isWritable()) {
            switch (getEndpointType()) {
                case dimmer -> actionBuilder = createDimmerActionBuilder(uiInputBuilder);
                case color -> actionBuilder = createColorActionBuilder(uiInputBuilder);
                case bool -> actionBuilder = createBoolActionBuilder(uiInputBuilder);
                case number -> actionBuilder = createNumberActionBuilder(uiInputBuilder);
                case select -> actionBuilder = createSelectActionBuilder(uiInputBuilder);
                case string -> actionBuilder = createStringActionBuilder(uiInputBuilder);
            }
        }
        if (actionBuilder != null) {
            return actionBuilder;
        }
        if (getUnit() != null) {
            uiInputBuilder.addInfo("%s <small class=\"text-muted\">%s</small>"
                    .formatted(value.stringValue(), getUnit()), InfoType.HTML);
        } else {
            assembleUIAction(uiInputBuilder);
        }
        return uiInputBuilder;
    }

    default UIInputBuilder createColorActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        throw new IllegalStateException("Must be implemented");
    }

    default UIInputBuilder createDimmerActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        throw new IllegalStateException("Must be implemented");
    }

    default UIInputBuilder createStringActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        if (!getValue().stringValue().equals("N/A")) {
            uiInputBuilder.addTextInput(getEntityID(), getValue().stringValue(), false).setApplyButton(true)
                    .setDisabled(isDisabled());
            return uiInputBuilder;
        }
        return null;
    }

    default UIInputBuilder createSelectActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder
                .addSelectBox(getEntityID(), (entityContext, params) -> {
                    setValue(new StringType(params.getString("value")), false);
                    return onExternalUpdated();
                })
                .addOptions(OptionModel.list(getSelectValues()))
                .setPlaceholder("-----------")
                .setSelected(getValue().toString())
                .setDisabled(isDisabled());
        return uiInputBuilder;
    }

    default UIInputBuilder createNumberActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addSlider(getEntityID(), getValue().floatValue(0), getMin(), getMax(),
                (entityContext, params) -> {
                    setValue(new DecimalType(params.getInt("value")), false);
                    return onExternalUpdated();
                }).setDisabled(isDisabled());
        return uiInputBuilder;
    }

    default UIInputBuilder createBoolActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addCheckbox(getEntityID(), getValue().boolValue(), (entityContext, params) -> {
            setValue(OnOffType.of(params.getBoolean("value")), false);
            return onExternalUpdated();
        }).setDisabled(isDisabled());
        return uiInputBuilder;
    }

    default @Nullable ActionResponseModel onExternalUpdated() {
        throw new NotImplementedException("Method must be implemented in sub class if calls");
    }

    boolean isDisabled();

    default void assembleUIAction(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addInfo(getValue().toString(), InfoType.Text);
    }

    /**
     * Uses to create settings for single endpoint
     *
     * @return settings builder
     */
    default @Nullable UIInputBuilder createSettingsBuilder() {
        return null;
    }


    @Getter
    @RequiredArgsConstructor
    enum EndpointType {
        bool((jsonObject, s) -> OnOffType.of(jsonObject.getBoolean(s)), State::boolValue),
        number((jsonObject, s) -> new DecimalType(jsonObject.getInt(s)), State::floatValue),
        dimmer((jsonObject, s) -> new DecimalType(jsonObject.getInt(s)), State::intValue),
        string((jsonObject, s) -> new StringType(jsonObject.getString(s)), State::stringValue),
        select((jsonObject, s) -> new StringType(jsonObject.getString(s)), State::stringValue),
        color((jsonObject, s) -> new StringType(jsonObject.getString(s)), State::stringValue);

        private final BiFunction<JSONObject, String, State> reader;
        private final Function<State, Object> fromStateConverter;
    }
}
