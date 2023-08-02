package org.homio.api.model.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.UIField;
import org.homio.api.ui.field.action.v1.UIInputEntity;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
public class DeviceEndpointUI implements Comparable<DeviceEndpointUI> {

    private String entityID;

    @UIField(order = 2)
    private EndpointNode node;

    @JsonIgnore
    private DeviceEndpoint endpoint;

    public DeviceEndpointUI(DeviceEndpoint endpoint) {
        this.endpoint = endpoint;
        this.entityID = endpoint.getEntityID();
        String varSource = endpoint.getVariableID() == null ? null : endpoint.getEntityContext().var().buildDataSource(endpoint.getVariableID(), false);
        node = new EndpointNode(endpoint.getIcon(), endpoint.getName(false), endpoint.getDescription(), varSource,
            endpoint.createUIInputBuilder().buildAll());
    }

    public static List<DeviceEndpointUI> buildEndpoints(Collection<DeviceEndpoint> entities) {
        return entities.stream()
                       .filter(DeviceEndpoint::isVisible)
                       .map(DeviceEndpointUI::new)
                       .sorted()
                       .collect(Collectors.toList());
    }

    @Override
    public int compareTo(@NotNull DeviceEndpointUI o) {
        return endpoint.compareTo(o.endpoint);
    }

    public record EndpointNode(
        Icon icon,
        String title,
        String description,
        String varSource,
        Collection<UIInputEntity> actions) {
    }
}
