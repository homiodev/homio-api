package org.homio.api.model.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.homio.api.Context;
import org.homio.api.ContextVar;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.model.Status;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.item.UICheckboxItemBuilder;
import org.homio.api.ui.field.action.v1.item.UIInfoItemBuilder.InfoType;
import org.homio.api.ui.field.action.v1.item.UISelectBoxItemBuilder;
import org.homio.api.ui.field.action.v1.item.UISliderItemBuilder;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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

    @NotNull
    String getEndpointName();

    /**
     * @param shortFormat -
     * @return Property human representation name
     */
    @NotNull
    String getName(boolean shortFormat);

    @Nullable
    String getDescription();

    @NotNull
    Icon getIcon();

    @Nullable
    String getUnit();

    @Nullable
    String getVariableID();

    @Nullable
    ContextVar.VariableType getVariableType();

    /**
     * It's unique device id. it should be same even if user recreate camera from scratch
     *
     * @return device id
     */
    @NotNull
    String getDeviceID();

    @NotNull
    String getEndpointEntityID();

    default @NotNull String getEntityID() {
        return getDeviceID() + "_" + getEndpointEntityID();
    }

    /**
     * @return Last read value
     */
    @NotNull
    State getLastValue();

    /**
     * @return duration since last event
     */
    @NotNull
    Duration getTimeSinceLastEvent();

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

    @NotNull
    DeviceEndpoint.EndpointType getEndpointType();

    /**
     * Implement by property that has ability to read value.
     */
    default void readValue() {
        if (isReadable()) {
            throw new IllegalStateException("Method must be implemented by readable property");
        }
        throw new NotImplementedException();
    }

    /**
     * Implement by property that has ability to write value. Calls external i.e. by scratch block
     *
     * @param state - value to set. May be need convert value to correct format
     */
    default void writeValue(@NotNull State state) {
        throw new IllegalStateException("Must be implemented for endpoint that has write ability");
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

    default @NotNull List<OptionModel> getSelectValues() {
        throw new IllegalStateException("Must be implemented for 'select' type");
    }

    default @Nullable Object getDefaultValue() {
        return null;
    }

    default @NotNull Float getMin() {
        throw new IllegalStateException("Must be implemented for 'slider' type");
    }

    default @NotNull Float getMax() {
        throw new IllegalStateException("Must be implemented for 'slider' type");
    }

    @NotNull
    Context context();

    @NotNull
    State getValue();

    /**
     * Update endpoint value
     *
     * @param value          - new value
     * @param externalUpdate - if need update state on UI
     */
    void setValue(State value, boolean externalUpdate);

    /**
     * Uses to create action(s)(i.e. slider/switch/text) for endpoint row
     *
     * @return action builder
     */
    default @Nullable UIInputBuilder createActionBuilder() {
        UIInputBuilder uiInputBuilder = context().ui().inputBuilder();
        State value = getValue();
        UIInputBuilder actionBuilder = null;

        if (isWritable()) {
            switch (getEndpointType()) {
                case dimmer -> actionBuilder = createDimmerActionBuilder(uiInputBuilder);
                case color -> actionBuilder = createColorActionBuilder(uiInputBuilder);
                case bool -> actionBuilder = createBoolActionBuilder(uiInputBuilder);
                case number -> actionBuilder = createSliderActionBuilder(uiInputBuilder);
                case trigger -> actionBuilder = createTriggerActionBuilder(uiInputBuilder);
                case select -> actionBuilder = createSelectActionBuilder(uiInputBuilder);
                case string -> actionBuilder = createStringActionBuilder(uiInputBuilder);
            }
        }
        if (actionBuilder != null) {
            return actionBuilder;
        }
        if (getUnit() != null) {
            uiInputBuilder.addInfo("%s <small>%s</small>"
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
        return createSliderActionBuilder(uiInputBuilder);
    }

    default UIInputBuilder createStringActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addTextInput(getEntityID(), getValue().stringValue(), false).setRequireApply(true)
                .setDisabled(isDisabled());
        return uiInputBuilder;
    }

    default UIInputBuilder createSelectActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        List<OptionModel> options = createSelectActionOptions();
        Object defaultValue = getDefaultValue();
        if (defaultValue != null) {
            for (OptionModel option : options) {
                if (option.getKey().equals(defaultValue)) {
                    option.setTitle(option.getTitleOrKey() + Lang.getServerMessage("OPTION_DEFAULT"));
                }
            }
        }

        postConfigureSelectBoxAction(uiInputBuilder
                .addSelectBox(getEntityID(), (context, params) -> {
                    setValue(new StringType(params.getString("value")), false);
                    return onExternalUpdated();
                })
                .addOptions(options)
                .setPlaceholder("-----------")
                .setHighlightSelected(true)
                .setSelected(getValue().toString())
                .setDisabled(isDisabled()));
        return uiInputBuilder;
    }

    default void postConfigureSelectBoxAction(UISelectBoxItemBuilder value) {

    }

    default void postConfigureBoolAction(UICheckboxItemBuilder value) {

    }

    default void postConfigureSliderAction(UISliderItemBuilder value) {

    }

    /**
     * Able to override to customize options for selectBox
     *
     * @return selectBox options
     */
    default @NotNull List<OptionModel> createSelectActionOptions() {
        return getSelectValues();
    }

    default UIInputBuilder createSliderActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        float value = getValue().floatValue(0);
        UISliderItemBuilder sliderItemBuilder =
                uiInputBuilder.addSlider(getEntityID(), value, getMin(), getMax(),
                                (context, params) -> {
                                    setValue(new DecimalType(params.getInt("value")), false);
                                    return onExternalUpdated();
                                })
                        .setDefaultValue((Float) getDefaultValue())
                        .setThumbLabel(getUnit())
                        .setDisabled(isDisabled());
        postConfigureSliderAction(sliderItemBuilder);
        return uiInputBuilder;
    }

    default UIInputBuilder createTriggerActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addButton(this.getEntityID(), getIcon(), (context, params) -> {
            onExternalUpdated();
            return null;
        }).setText("");
        return uiInputBuilder;
    }

    default UIInputBuilder createBoolActionBuilder(@NotNull UIInputBuilder uiInputBuilder) {
        postConfigureBoolAction(uiInputBuilder.addCheckbox(getEntityID(), getValue().boolValue(), (context, params) -> {
            setValue(OnOffType.of(params.getBoolean("value")), false);
            return onExternalUpdated();
        }).setDisabled(isDisabled()));
        return uiInputBuilder;
    }

    default @Nullable ActionResponseModel onExternalUpdated() {
        throw new NotImplementedException("Method must be implemented in sub class if calls");
    }

    boolean isDisabled();

    boolean isStateless();

    default void assembleUIAction(@NotNull UIInputBuilder uiInputBuilder) {
        switch (getEndpointEntityID()) {
            case ENDPOINT_SIGNAL:
            case ENDPOINT_BATTERY:
                int val = getValue().intValue();
                uiInputBuilder.addInfo(getValue().toString(), InfoType.Text)
                        .setColor(val < 35 ? "#E74C3C" : (val < 50) ? "#EC8826" : (val < 85) ? "#F1C40F" : "#8BC34A");
                break;
            case ENDPOINT_DEVICE_STATUS:
                Status status = Status.valueOf(getValue().stringValue());
                uiInputBuilder.addInfo(status.name(), InfoType.Text).setColor(status.getColor());
                break;
            case ENDPOINT_LAST_SEEN:
                long value = getValue().longValue();
                String color = value > 600000 ? "#E74C3C" : (value > 300000 ? "#EC8826" : "#8BC34A");
                uiInputBuilder.addDuration(value, color);
                break;
            default:
                uiInputBuilder.addInfo(getValue().toString(), InfoType.Text);
        }
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
        trigger(
                (jsonObject, s) -> new StringType(jsonObject.getString(s)),
                jsonNode -> new StringType(jsonNode.asText()),
                State::stringValue),
        bool(
                (jsonObject, s) -> OnOffType.of(jsonObject.getBoolean(s)),
                jsonNode -> OnOffType.of(jsonNode.asBoolean()),
                State::boolValue),
        // Number for float numbers
        number(
                (jsonObject, s) -> new DecimalType(jsonObject.getInt(s)),
                jsonNode -> new DecimalType(jsonNode.asDouble()),
                State::floatValue),
        // Dimmer for int values
        dimmer(
                (jsonObject, s) -> new DecimalType(jsonObject.getInt(s)),
                jsonNode -> new DecimalType(jsonNode.asText()),
                State::intValue),
        string(
                (jsonObject, s) -> new StringType(jsonObject.getString(s)),
                jsonNode -> new StringType(jsonNode.asText()),
                State::stringValue),
        select(
                (jsonObject, s) -> new StringType(jsonObject.getString(s)),
                jsonNode -> new StringType(jsonNode.asText()),
                State::stringValue),
        color(
                (jsonObject, s) -> new StringType(jsonObject.getString(s)),
                jsonNode -> new StringType(jsonNode.asText()),
                State::stringValue);

        private final BiFunction<JSONObject, String, State> reader;
        private final Function<JsonNode, State> nodeReader;
        private final Function<State, Object> fromStateConverter;
    }
}
