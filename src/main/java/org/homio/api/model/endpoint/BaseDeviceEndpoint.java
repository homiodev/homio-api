package org.homio.api.model.endpoint;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextVar.VariableMetaBuilder;
import org.homio.api.EntityContextVar.VariableType;
import org.homio.api.entity.device.DeviceEndpointsBehaviourContract;
import org.homio.api.model.Icon;
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
    private final @Getter @NotNull EntityContext entityContext;

    private @Getter @Setter Icon icon;
    private @Getter @Setter String endpointEntityID;

    private @Getter D device;

    private @Getter @Nullable String unit;
    private @Getter long updated;
    private @Getter @NotNull State value = new StringType("N/A");
    private @Nullable Object dbValue;
    private @Nullable @Getter String variableID;
    private @Getter @Setter boolean readable = true;
    private @Getter @Setter boolean writable = true;
    private @Getter @Setter String endpointName;
    private @Getter @Setter EndpointType endpointType;
    private @Getter int order;

    private @Nullable ConfigDeviceDefinitionService configService;
    private @Getter @Setter @Nullable Float min;
    private @Getter @Setter @Nullable Float max;
    private @Getter @Setter @Nullable Set<String> range;
    private @Getter @Setter @Nullable Object defaultValue;
    private WriteHandler writeHandler;
    private @JsonIgnore @Nullable Set<String> alternateEndpoints;
    private @Setter @Nullable ConfigDeviceEndpoint configDeviceEndpoint;
    private @Getter @Setter boolean visibleEndpoint = true;

    public BaseDeviceEndpoint(@NotNull Icon icon, @NotNull String group, @NotNull EntityContext entityContext) {
        this(group, entityContext);
        this.icon = icon;
    }

    public BaseDeviceEndpoint(
        @NotNull Icon icon,
        @NotNull String group,
        @NotNull EntityContext entityContext,
        @NotNull D device,
        @NotNull String endpointEntityID,
        boolean writable,
        @NotNull EndpointType endpointType) {
        this(icon, group, entityContext);
        setInitial(device, endpointEntityID, writable, endpointType);
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
    public @NotNull Set<String> getSelectValues() {
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

    public void setValue(@Nullable State value, boolean externalUpdate) {
        if (value != null && !this.value.equals(value)) {
            this.value = value;
            this.updated = System.currentTimeMillis();
            for (Consumer<State> changeListener : changeListeners.values()) {
                changeListener.accept(getValue());
            }
            pushVariable();
            if (externalUpdate) {
                updateUI();
            }
        }
    }

    @Override
    public boolean isDisabled() {
        return !device.getStatus().isOnline();
    }

    public void init(
        @NotNull ConfigDeviceDefinitionService configService,
        @Nullable String endpointEntityID,
        @NotNull D device,
        @Nullable String unit,
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
        this.endpointName = endpointName;
        this.unit = StringUtils.isEmpty(unit) ? configDeviceEndpoint == null ? null : configDeviceEndpoint.getUnit() : unit;
        this.readable = readable;
        setInitial(device, endpointEntityID, writable, endpointType);

        order = configDeviceEndpoint == null ? 0 : configDeviceEndpoint.getOrder();
        if (order == 0) {
            order = endpointName.charAt(0) * 10 + endpointName.charAt(1);
        }

        boolean createVariable = configDeviceEndpoint == null || !configDeviceEndpoint.isStateless();
        if (createVariable) {
            getOrCreateVariable();
        }

        this.writeHandler = createExternalWriteHandler();
    }

    @Override
    public boolean isVisible() {
        if (configService != null && configService.isHideEndpoint(getEndpointEntityID())) {
            return false;
        }
        if (!visibleEndpoint) {
            return false;
        }
        return !getHiddenEndpoints().contains(getEndpointEntityID());
    }

    public @NotNull Set<String> getHiddenEndpoints() {
        return Set.of();
    }

    public @NotNull String getDeviceEntityID() {
        return device.getEntityID();
    }

    public @NotNull String getDeviceID() {
        return requireNonNull(defaultIfEmpty(device.getIeeeAddress(), device.getEntityID()));
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

    public boolean writeValue(Object rawValue, boolean externalUpdate) {
        return writeHandler.write(rawValue, externalUpdate);
    }

    protected void pushVariable() {
        if (variableID != null) {
            entityContext.var().set(variableID, value, dbValue -> this.dbValue = dbValue);
        }
    }

    /**
     * Fire ui updated when endpoint value changed
     */
    protected void updateUI() {
        entityContext.ui().updateInnerSetItem(device, "endpoints",
            endpointEntityID, getEntityID(), new DeviceEndpointUI(this));
    }

    public @Nullable String getOrCreateVariable() {
        if (variableID == null) {
            VariableType variableType = getVariableType();
            boolean persistent = configDeviceEndpoint != null && configDeviceEndpoint.isPersistent();
            Consumer<VariableMetaBuilder> customVariableMetaBuilder = getVariableMetaBuilder();
            Consumer<VariableMetaBuilder> variableMetaBuilder = builder -> {
                builder.setIcon(icon);
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
                variableID = entityContext.var().createEnumVariable(getDeviceID(),
                    getEntityID(), getName(false), getVariableEnumValues(), variableMetaBuilder);
            } else {
                variableID = entityContext.var().createVariable(getDeviceID(),
                    getEntityID(), getName(false), variableType, variableMetaBuilder);
            }

            if (isWritable()) {
                entityContext.var().setLinkListener(requireNonNull(variableID), varValue -> {
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

    protected @NotNull Set<String> getVariableEnumValues() {
        if (range == null) {
            throw new IllegalStateException("Property with enum variable must override this method");
        }
        return range;
    }

    protected @Nullable Consumer<VariableMetaBuilder> getVariableMetaBuilder() {
        return builder -> {
            builder.setDescription(getVariableDescription())
                   .setReadOnly(!isWritable())
                   .setColor(getIcon().getColor());
            List<String> attributes = new ArrayList<>();
            if (min != null) {
                attributes.add("min:" + min);
            }
            if (max != null) {
                attributes.add("max:" + max);
            }
            if (range != null && !range.isEmpty()) {
                attributes.add("range:" + String.join(";", range));
            }
            builder.setAttributes(attributes);
        };
    }

    protected String getVariableDescription() {
        List<String> description = new ArrayList<>();
        description.add(getDescription());
        if (range != null && !range.isEmpty()) {
            description.add("(range:%s)".formatted(String.join(";", range)));
        }
        if (min != null && max != null) {
            description.add("(min-max:%S...%s)".formatted(min, max));
        }
        return String.join(" ", description);
    }

    protected @NotNull VariableType getVariableType() {
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

    protected WriteHandler createExternalWriteHandler() {
        switch (endpointType) {
            case bool -> {
                return (rawValue, eu) -> {
                    if (Boolean.class.isAssignableFrom(rawValue.getClass())) {
                        setValue(OnOffType.of((boolean) rawValue), eu);
                        return true;
                    }
                    return false;
                };
            }
            case number -> {
                return (rawValue, eu) -> {
                    if (Number.class.isAssignableFrom(rawValue.getClass())) {
                        setValue(new DecimalType((Number) rawValue), eu);
                        return true;
                    }
                    return false;
                };
            }
            case color -> {
                return (rawValue, eu) -> {
                    if (rawValue instanceof String) {
                        setValue(new StringType(decodeColor((String) rawValue)), eu);
                        return true;
                    }
                    return false;
                };
            }
            case dimmer -> {
                return (rawValue, eu) -> {
                    if (Double.class.isAssignableFrom(rawValue.getClass())) {
                        double value = (double) rawValue;
                        if (value <= getMin()) {
                            setValue(new DecimalType(getMin()), eu);
                        } else if (value >= getMax()) {
                            setValue(new DecimalType(getMax()), eu);
                        } else {
                            setValue(new DecimalType(new BigDecimal(100.0 * value / (getMax() - 0))), eu);
                        }
                        return true;
                    }
                    return false;
                };
            }
            default -> {
                // select, string, trigger
                return (rawValue, eu) -> {
                    if (rawValue instanceof String) {
                        setValue(new StringType((String) rawValue), eu);
                        return true;
                    }
                    return false;
                };
            }
        }
    }

    protected String decodeColor(String value) {
        return value;
    }

    private interface WriteHandler {

        boolean write(Object value, boolean externalUpdate);
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
