package org.homio.api.model.endpoint;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextVar.VariableMetaBuilder;
import org.homio.api.EntityContextVar.VariableType;
import org.homio.api.entity.DeviceEndpointsBaseEntity;
import org.homio.api.model.Icon;
import org.homio.api.model.device.ConfigDeviceDefinitionService;
import org.homio.api.model.device.ConfigDeviceEndpoint;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.item.UIInfoItemBuilder.InfoType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public abstract class BaseDeviceEndpoint<D extends DeviceEndpointsBaseEntity> implements DeviceEndpoint {

    private final @NotNull Map<String, Consumer<State>> changeListeners = new ConcurrentHashMap<>();

    private final @Getter @NotNull Icon icon;
    @Getter
    private String endpointEntityID;
    @Getter
    private D device;

    private @Getter @Nullable String unit;
    private @Getter long updated;
    private @Getter State value = new StringType("N/A");
    private @Nullable Object dbValue;
    private @Getter @Nullable String variableID;
    private @Getter @Setter boolean readable = true;
    private @Getter @Setter boolean writable = true;
    private @Getter String endpointName;
    private @Getter EndpointType endpointType;
    private @Getter @Setter int order;

    protected @Getter EntityContext entityContext;
    private @Getter @Setter @Nullable ConfigDeviceEndpoint configDeviceEndpoint;
    private ConfigDeviceDefinitionService configService;
    private @JsonIgnore @Nullable Set<String> alternateEndpoints;

    public BaseDeviceEndpoint(@NotNull Icon icon) {
        this.icon = icon;
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

    public void setValue(@NotNull State value, boolean externalUpdate) {
        if (!this.value.equals(value)) {
            this.value = value;
            if (externalUpdate) {
                this.updated = System.currentTimeMillis();
                for (Consumer<State> changeListener : changeListeners.values()) {
                    changeListener.accept(getValue());
                }
                updateUI();
                pushVariable();
            }
        }
    }

    public void init(
        @NotNull ConfigDeviceDefinitionService configService,
        @Nullable String endpointEntityID,
        @NotNull D device,
        @NotNull EntityContext entityContext,
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

        this.device = device;
        this.endpointName = endpointName;
        this.endpointEntityID = endpointEntityID;
        this.entityContext = entityContext;
        this.unit = StringUtils.isEmpty(unit) ? configDeviceEndpoint == null ? null : configDeviceEndpoint.getUnit() : unit;
        this.readable = readable;
        this.writable = writable;
        this.endpointType = endpointType;

        order = configDeviceEndpoint == null ? 0 : configDeviceEndpoint.getOrder();
        if (order == 0) {
            order = endpointName.charAt(0) * 10 + endpointName.charAt(1);
        }

        boolean createVariable = configDeviceEndpoint == null || !configDeviceEndpoint.isStateless();
        if (createVariable) {
            getOrCreateVariable();
        }
    }

    @Override
    public boolean isVisible() {
        if (configService.isHideEndpoint(getEndpointEntityID())) {
            return false;
        }
        return !getHiddenEndpoints().contains(getEndpointEntityID());
    }

    public abstract @NotNull List<String> getHiddenEndpoints();

    public @NotNull String getDeviceEntityID() {
        return device.getEntityID();
    }

    public @NotNull String getDeviceID() {
        return requireNonNull(device.getIeeeAddress());
    }

    public void assembleUIAction(@NotNull UIInputBuilder uiInputBuilder) {
        uiInputBuilder.addInfo(value.toString(), InfoType.Text);
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
            endpointEntityID, "value", new DeviceEndpointUI(this).getValue());
        entityContext.ui().updateInnerSetItem(device, "endpoints",
            endpointEntityID, "updated", updated);
    }

    protected void getOrCreateVariable() {
        if (variableID == null) {
            VariableType variableType = getVariableType();
            if (variableType == VariableType.Enum) {
                variableID = entityContext.var().createEnumVariable(getDeviceID(),
                    getEntityID(), getName(false), getVariableEnumValues(), getVariableMetaBuilder());
            } else {
                variableID = entityContext.var().createVariable(getDeviceID(),
                    getEntityID(), getName(false), variableType, getVariableMetaBuilder());
            }
            entityContext.var().setVariableIcon(variableID, icon);

            if (isWritable()) {
                entityContext.var().setLinkListener(requireNonNull(variableID), varValue -> {
                    if (!this.device.getStatus().isOnline()) {
                        throw new RuntimeException("Unable to handle property " + getVariableID() + " action. Device noy online");
                    }
                    // fire updates only if variable updates externally
                    if (!Objects.equals(dbValue, varValue)) {
                        writeValue(State.of(varValue));
                    }
                });
            }
        }
    }

    protected @NotNull List<String> getVariableEnumValues() {
        throw new IllegalStateException("Property with enum variable must override this method");
    }

    protected abstract @Nullable Consumer<VariableMetaBuilder> getVariableMetaBuilder();

    protected abstract @NotNull VariableType getVariableType();

    @Override
    public String toString() {
        return "Entity: " + getEntityID() + ". Order: " + getOrder();
    }
}
