package org.homio.api.model.endpoint;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import org.homio.api.EntityContext;
import org.homio.api.EntityContextVar.VariableMetaBuilder;
import org.homio.api.EntityContextVar.VariableType;
import org.homio.api.entity.DeviceBaseEntity;
import org.homio.api.entity.DeviceBaseEntity.HasEndpointsDevice;
import org.homio.api.model.Icon;
import org.homio.api.state.State;
import org.homio.api.state.StringType;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.item.UIInfoItemBuilder.InfoType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specify device single endpoint base class
 */
@Getter
public abstract class BaseDeviceEndpoint<D extends DeviceBaseEntity & HasEndpointsDevice> implements DeviceEndpoint {

    protected final @NotNull Map<String, Consumer<State>> changeListeners = new ConcurrentHashMap<>();

    private final @NotNull Icon icon;
    protected String endpointEntityID;
    protected String deviceEntityID;
    protected D device;

    @Setter protected @Nullable String unit;
    @Setter protected long updated;
    @Getter protected EntityContext entityContext;
    @Setter protected State value = new StringType("N/A");
    protected @Nullable Object dbValue;
    protected @Nullable String variableID;
    @Setter private boolean readable = true;
    @Setter private boolean writable = true;
    private String endpointName;
    private EndpointType endpointType;
    private int order;

    public BaseDeviceEndpoint(@NotNull Icon icon) {
        this.icon = icon;
    }

    public void init(
        @NotNull String endpointEntityID,
        @NotNull D device,
        @NotNull EntityContext entityContext,
        @Nullable String unit,
        boolean readable,
        boolean writable,
        @NotNull String endpointName,
        int order,
        EndpointType endpointType) {

        this.endpointName = endpointName;
        this.order = order;
        this.endpointEntityID = endpointEntityID;
        this.deviceEntityID = device.getIeeeAddress();
        this.entityContext = entityContext;
        this.unit = unit;
        this.readable = readable;
        this.writable = writable;
        this.endpointType = endpointType;
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
            String varID = deviceEntityID + "_" + endpointEntityID;
            if (variableType == VariableType.Enum) {
                variableID = entityContext.var().createEnumVariable(deviceEntityID,
                    varID, getName(false), getVariableEnumValues(), getVariableMetaBuilder());
            } else {
                variableID = entityContext.var().createVariable(deviceEntityID,
                    varID, getName(false), variableType, getVariableMetaBuilder());
            }
            entityContext.var().setVariableIcon(variableID, icon);

            if (isWritable()) {
                entityContext.var().setLinkListener(variableID, varValue -> {
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
}
