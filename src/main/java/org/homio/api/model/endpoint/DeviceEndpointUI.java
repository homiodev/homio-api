package org.homio.api.model.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.v1.UIInputBuilder;
import org.homio.api.ui.field.action.v1.UIInputEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class DeviceEndpointUI implements Comparable<DeviceEndpointUI> {

    @JsonIgnore
    private DeviceEndpoint endpoint;

    private @Nullable String varSource;

    public DeviceEndpointUI(@NotNull DeviceEndpoint endpoint) {
        this.endpoint = endpoint;
        this.varSource = endpoint.getVariableID() == null ? null :
                endpoint.getEntityContext().var().buildDataSource(endpoint.getVariableID(), false);
    }

    public static @NotNull List<DeviceEndpointUI> buildEndpoints(@NotNull Collection<? extends DeviceEndpoint> entities) {
        return entities.stream()
                .filter(DeviceEndpoint::isVisible)
                .map(DeviceEndpointUI::new)
                .sorted()
                .collect(Collectors.toList());
    }

    public @NotNull String getEntityID() {
        return endpoint.getEntityID();
    }

    public @NotNull Icon getIcon() {
        return endpoint.getIcon();
    }

    public @NotNull String getTitle() {
        return endpoint.getName(false);
    }

    public @Nullable String getDescription() {
        return endpoint.getDescription();
    }

    public @Nullable Collection<UIInputEntity> getActions() {
        UIInputBuilder builder = endpoint.createActionBuilder();
        return builder == null ? null : builder.buildAll();
    }

    public @Nullable Collection<UIInputEntity> getSettings() {
        UIInputBuilder builder = endpoint.createSettingsBuilder();
        return builder == null ? null : builder.buildAll();
    }

    @Override
    public int compareTo(@NotNull DeviceEndpointUI o) {
        return endpoint.compareTo(o.endpoint);
    }
}
