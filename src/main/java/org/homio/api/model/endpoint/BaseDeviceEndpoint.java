package org.homio.api.model.endpoint;

import static java.util.Objects.requireNonNull;
import static org.homio.api.util.CommonUtils.splitNameToReadableFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.ContextVar.VariableMetaBuilder;
import org.homio.api.ContextVar.VariableType;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.ActionResponseModel;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel;
import org.homio.api.model.Status;
import org.homio.api.model.device.ConfigDeviceDefinitionService;
import org.homio.api.model.device.ConfigDeviceEndpoint;
import org.homio.api.state.DecimalType;
import org.homio.api.state.OnOffType;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public abstract class BaseDeviceEndpoint<D extends DeviceEndpointsBehaviourContract> implements DeviceEndpoint {

    private final @NotNull Map<String, Consumer<State>> changeListeners = new ConcurrentHashMap<>();
    private final @NotNull @Getter String group;
    private final @Getter @Accessors(fluent = true) @NotNull Context context;

    private @Getter @Setter Icon icon;
    private @Getter @Setter String endpointEntityID;

    private @Getter D device;

    private @Getter @Setter @Nullable String unit;
    private @Getter long updated;
    private @Getter @NotNull State value = new StringType("N/A");
    private @Nullable Object dbValue;
    private @Nullable @Getter String variableID;
    private @Getter @Setter boolean readable = true;
    private @Getter @Setter boolean writable = true;
    private @Getter @Setter String endpointName;
    private @Getter @Setter EndpointType endpointType;
    private @Getter @Setter int order = -1;

    private @Nullable ConfigDeviceDefinitionService configService;
    private @Getter @Setter @Nullable Float min;
    private @Getter @Setter @Nullable Float max;
    private @Getter @Setter @Nullable List<OptionModel> range;
    private @Getter @Setter @Nullable Object defaultValue;
    private @JsonIgnore @Nullable Set<String> alternateEndpoints;
    private @Setter @Nullable ConfigDeviceEndpoint configDeviceEndpoint;
    private @Getter @Setter boolean visibleEndpoint = true;
    private @Getter @Setter Supplier<Boolean> visibleEndpointHandler;
    private @Setter boolean ignoreDuplicates = true;
    private @Getter @Setter boolean stateless;
    private @Nullable Consumer<State> updateHandler;
    private @Setter boolean dbValueStorable;

    public BaseDeviceEndpoint(@NotNull Icon icon, @NotNull String group, @NotNull Context context) {
        this(group, context);
        this.icon = icon;
    }

    public BaseDeviceEndpoint(
        @NotNull Icon icon,
        @NotNull String group,
        @NotNull Context context,
        @NotNull D device,
        @NotNull String endpointEntityID,
        boolean writable,
        @NotNull EndpointType endpointType) {
        this(icon, group, context);
        setInitial(device, endpointEntityID, writable, endpointType);
    }

    public void init(
        @NotNull ConfigDeviceDefinitionService configService,
        @Nullable String endpointEntityID,
        @NotNull D device,
        boolean readable,
        boolean writable,
        @Nullable String endpointName,
        @NotNull EndpointType endpointType) {

        this.configService = configService;
        configDeviceEndpoint = endpointEntityID == null ? null : configService.getDeviceEndpoints().get(endpointEntityID);
        if (configDeviceEndpoint == null && alternateEndpoints != null) {
            for (String alternativeEndpointEntityId : alternateEndpoints) {
                configDeviceEndpoint = configService.getDeviceEndpoints().get(alternativeEndpointEntityId);
                if (configDeviceEndpoint != null) {
                    endpointEntityID = alternativeEndpointEntityId;
                    break;
                }
            }
        }

        if (endpointName == null) {
            endpointName = endpointEntityID;
        }

        if (endpointEntityID == null) {
            throw new IllegalStateException("Unable to create device endpoint without endpoint id. " + endpointName);
        }
        switch (endpointEntityID) {
            case ENDPOINT_DEVICE_STATUS:
                icon = new Icon("fa fa-globe", "#42B52D");
                order = 10;
                ignoreDuplicates = true;
                setInitialValue(new StringType(Status.UNKNOWN.name()));
                break;
            case ENDPOINT_LAST_SEEN:
                icon = new Icon("fa fa-eye", "#2D9C2C");
                setInitialValue(new DecimalType(System.currentTimeMillis()));
                break;
            case ENDPOINT_BATTERY:
                icon = new Icon("fa fa-battery-full", "#32D1B9");
                min = 0F;
                max = 100F;
                break;
            case ENDPOINT_SIGNAL:
                icon = new Icon("fa fa-signal", "#D134AF");
                min = 0F;
                max = 100F;
                break;
        }
        this.endpointName = endpointName;
        if (this.unit == null) {
            this.unit = configDeviceEndpoint == null ? null : configDeviceEndpoint.getUnit();
        }
        if (configDeviceEndpoint != null && configDeviceEndpoint.getIgnoreDuplicates() != null) {
            ignoreDuplicates = configDeviceEndpoint.getIgnoreDuplicates();
        }
        if (configDeviceEndpoint != null) {
            this.min = this.min == null ? configDeviceEndpoint.getMin() : this.min;
            this.max = this.max == null ? configDeviceEndpoint.getMax() : this.max;
        }
        this.readable = readable;
        setInitial(device, endpointEntityID, writable, endpointType);

        if (order == -1) {
            order = configDeviceEndpoint == null ? 0 : configDeviceEndpoint.getOrder();
            if (order == 0) {
                order = endpointName.charAt(0) * 10 + endpointName.charAt(1);
            }
        }

        stateless = configDeviceEndpoint != null && configDeviceEndpoint.isStateless();
    }

    public void setUpdateHandler(Consumer<State> updateHandler) {
        this.updateHandler = updateHandler;
        if (updateHandler != null && endpointType != EndpointType.trigger) {
            this.writable = true;
        }
    }

    @Override
    public void writeValue(@NotNull State state) {
        State targetState;
        Object targetValue;
        switch (getEndpointType()) {
            case bool -> {
                targetState = state instanceof OnOffType ? state : OnOffType.of(state.boolValue());
                targetValue = targetState.boolValue();
            }
            case number, dimmer -> {
                targetState = state instanceof DecimalType ? state : new DecimalType(state.intValue());
                targetValue = state.floatValue();
            }
            default -> {
                targetState = state;
                targetValue = state.stringValue();
            }
        }
        setValue(targetState, true);
        if (dbValueStorable) {
            getDevice().setJsonData(getEndpointEntityID(), targetValue);
            context().db().save((BaseEntity) getDevice());
        }
        if (updateHandler != null) {
            updateHandler.accept(targetState);
        }
    }

    @Override
    public @Nullable ActionResponseModel onExternalUpdated() {
        if (updateHandler == null) {
            throw new IllegalStateException("No update handler set for write handler: " + getEntityID());
        }
        updateHandler.accept(getValue());
        return null;
    }

    @Override
    public @NotNull String getName(boolean shortFormat) {
        String l1Name = getEndpointEntityID();
        String name = splitNameToReadableFormat(l1Name);
        return shortFormat ? name : "${%s_N.%s~%s}".formatted(group, l1Name, name);
    }

    @Override
    public @Nullable String getDescription() {
        return "${%s_D.%s~%s}".formatted(group, endpointEntityID, endpointEntityID);
    }

    @Override
    public @NotNull List<OptionModel> getSelectValues() {
        if (range != null) {
            return range;
        }
        // throw error if not defined
        return DeviceEndpoint.super.getSelectValues();
    }

    /**
     * Set list of alternative endpoint names if endpointEntityID is null or if configService.getDeviceEndpoints().get(endpointEntityID) == null
     *
     * @param alternateEndpoints list if string(nullable)
     */
    public void setAlternateEndpoints(@Nullable String... alternateEndpoints) {
        this.alternateEndpoints = new HashSet<>();
        for (String endpoint : alternateEndpoints) {
            if (endpoint != null) {
                this.alternateEndpoints.add(endpoint);
            }
        }
    }

    public void setInitialValue(State value) {
        this.value = value;
    }

    public void setValue(@Nullable State value, boolean externalUpdate) {
        if (value == null) {return;}
        if (this.value.equals(value) && ignoreDuplicates) {return;}

        this.value = value;
        this.updated = System.currentTimeMillis();
        for (Consumer<State> changeListener : changeListeners.values()) {
            changeListener.accept(getValue());
        }
        pushVariable();
        if (ignoreDuplicates) {
            context.event().fireEvent(getEntityID(), value);
        } else {
            context.event().fireEventIfNotSame(getEntityID(), value);
        }
        if (externalUpdate) {
            updateUI();
        }
    }

    @Override
    public boolean isDisabled() {
        return !device.getStatus().isOnline();
    }

    private void setInitial(D device, String endpointEntityID, boolean writable, EndpointType endpointType) {
        this.device = device;
        this.endpointEntityID = endpointEntityID;
        this.writable = writable;
        this.endpointType = endpointType;
        if (endpointType == EndpointType.trigger) {
            this.readable = false;
            this.writable = true;
        }
        if (endpointType == EndpointType.bool) {
            value = OnOffType.OFF;
        } else if (endpointType == EndpointType.number || endpointType == EndpointType.dimmer) {
            value = DecimalType.ZERO;
        }
    }

    @Override
    public boolean isVisible() {
        if (configService != null && configService.isHideEndpoint(getEndpointEntityID())) {
            return false;
        }
        if (!visibleEndpoint) {
            return false;
        }
        if (visibleEndpointHandler != null) {
            return visibleEndpointHandler.get();
        }
        return !getHiddenEndpoints().contains(getEndpointEntityID());
    }

    public @NotNull Set<String> getHiddenEndpoints() {
        return Set.of();
    }

    public @NotNull String getDeviceEntityID() {
        return device.getEntityID();
    }

    /**
     * IeeeAddress may be empty when we create item from scratch
     *
     * @return device unique entity ID. Good to be same entity even on recreate item
     */
    public final @NotNull String getDeviceID() {
        return requireNonNull(StringUtils.defaultIfEmpty(device.getIeeeAddress(), device.getEntityID()));
    }

    @Override
    public @NotNull State getLastValue() {
        return value;
    }

    @Override
    public @NotNull Duration getTimeSinceLastEvent() {
        return Duration.ofMillis(System.currentTimeMillis() - updated);
    }

    @Override
    public void addChangeListener(@NotNull String id, @NotNull Consumer<State> changeListener) {
        changeListeners.put(id, changeListener);
    }

    @Override
    public void removeChangeListener(@NotNull String id) {
        changeListeners.remove(id);
    }

    @Override
    public String toString() {
        return "Entity: " + getEntityID() + ". Order: " + getOrder();
    }

    public @Nullable String getOrCreateVariable() {
        if (stateless) {
            return null;
        }
        if (variableID == null) {
            VariableType variableType = getVariableType();
            boolean persistent = configDeviceEndpoint != null && configDeviceEndpoint.isPersistent();
            Consumer<VariableMetaBuilder> customVariableMetaBuilder = getVariableMetaBuilder();
            Consumer<VariableMetaBuilder> variableMetaBuilder = builder -> {
                builder.setIcon(icon).setLocked(true);
                Integer quota = configDeviceEndpoint == null ? null : configDeviceEndpoint.getQuota();
                if (quota != null) {
                    builder.setQuota(quota);
                }
                if (customVariableMetaBuilder != null) {
                    customVariableMetaBuilder.accept(builder);
                }
                if (persistent) {
                    builder.setPersistent(true);
                }
            };
            if (variableType == VariableType.Enum) {
                Set<String> range = getVariableEnumValues().stream().map(OptionModel::getKey).collect(Collectors.toSet());
                variableID = context.var().createEnumVariable(getVariableGroupID(),
                    getEntityID(), getName(false), range, variableMetaBuilder);
            } else {
                variableID = context.var().createVariable(getVariableGroupID(),
                    getEntityID(), getName(false), variableType, variableMetaBuilder);
            }

            if (isWritable()) {
                context.var().setLinkListener(requireNonNull(variableID), varValue -> {
                    if (!this.device.getStatus().isOnline()) {
                        throw new RuntimeException("Unable to handle property " + getVariableID() + " actio. Device noy online");
                    }
                    // fire updates only if variable updates externally
                    if (!Objects.equals(dbValue, varValue)) {
                        writeValue(State.of(varValue));
                    }
                });
            }
        }
        return variableID;
    }

    protected void pushVariable() {
        if (variableID != null) {
            // we shouldn't fire link listener because it's fire writeValue to same endpoint infinite
            this.dbValue = context.var().set(variableID, value, false);
        }
    }

    /**
     * Fire ui updated when endpoint value changed
     */
    protected void updateUI() {
        context.ui().updateInnerSetItem(device, "endpoints",
            endpointEntityID, getEntityID(), new DeviceEndpointUI(this));
    }

    public String getVariableGroupID() {
        throw new IllegalStateException("Variable group id must be implemented in sub class if create variable");
    }

    protected @NotNull List<OptionModel> getVariableEnumValues() {
        if (range == null) {
            throw new IllegalStateException("Property with enum variable must override this method");
        }
        return range;
    }

    protected @Nullable Consumer<VariableMetaBuilder> getVariableMetaBuilder() {
        return builder -> {
            builder.setWritable(isWritable())
                   .setDescription(getVariableDescription())
                   .setColor(getIcon().getColor());
            List<String> attributes = new ArrayList<>();
            if (min != null || max != null) {
                builder.setNumberRange(min == null ? 0 : min, max == null ? Integer.MAX_VALUE : max);
            }
            if (range != null && !range.isEmpty()) {
                attributes.add("range:" + range.stream().map(OptionModel::getTitleOrKey).collect(Collectors.joining(";")));
            }
            builder.setAttributes(attributes);
        };
    }

    protected String getVariableDescription() {
        List<String> description = new ArrayList<>();
        description.add(getDescription());
        if (range != null && !range.isEmpty()) {
            description.add("(range:%s)".formatted(range.stream().map(OptionModel::getTitleOrKey).collect(Collectors.joining(";"))));
        }
        if (min != null && max != null) {
            description.add("(min-max:%S...%s)".formatted(min, max));
        }
        return String.join(" ", description);
    }

    @Override
    public @NotNull VariableType getVariableType() {
        switch (endpointType) {
            case bool, trigger -> {
                return VariableType.Bool;
            }
            case number, dimmer -> {
                return VariableType.Float;
            }
            case select -> {
                return VariableType.Enum;
            }
            case color -> {
                return VariableType.Color;
            }
            default -> {
                return VariableType.Any;
            }
        }
    }

    // helper method in case if we got external update of unknown type
    public @Nullable State rawValueToState(Object rawValue) {
        switch (endpointType) {
            case bool -> {
                if (Boolean.class.isAssignableFrom(rawValue.getClass())) {
                    return OnOffType.of((boolean) rawValue);
                }
            }
            case number -> {
                if (Number.class.isAssignableFrom(rawValue.getClass())) {
                    return new DecimalType((Number) rawValue);
                }
            }
            case color -> {
                return new StringType(decodeColor((String) rawValue));
            }
            case dimmer -> {
                if (Double.class.isAssignableFrom(rawValue.getClass())) {
                    double value = (double) rawValue;
                    if (getMin() != null && value <= getMin()) {
                        return new DecimalType(getMin());
                    } else if (getMax() != null && value >= getMax()) {
                        return new DecimalType(getMax());
                    } else {
                        float max = getMax() == null ? 1 : getMax();
                        return new DecimalType(new BigDecimal(100.0 * value / (max - 0)));
                    }
                }
            }
            default -> {
                // select, string, trigger
                return new StringType((String) rawValue);
            }
        }
        return null;
    }

    protected String decodeColor(String value) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        BaseDeviceEndpoint<?> that = (BaseDeviceEndpoint<?>) o;
        return Objects.equals(endpointEntityID, that.endpointEntityID);
    }

    @Override
    public int hashCode() {
        return endpointEntityID != null ? endpointEntityID.hashCode() : 0;
    }
}
