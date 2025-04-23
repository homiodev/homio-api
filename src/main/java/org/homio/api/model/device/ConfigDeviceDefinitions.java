package org.homio.api.model.device;

import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfigDeviceDefinitions {

    // not uses for now
    private int version;
    private List<ConfigDeviceDefinition> devices = List.of();
    // full list of all possible endpoints that contains endpoint icon/color/etc...
    private List<ConfigDeviceEndpoint> endpoints = List.of();
    // set of endpoints hide from UI
    private Set<String> hiddenEndpoints = Set.of();
    // set of endpoints that should be fully ignored
    private Set<String> ignoreEndpoints;
}
