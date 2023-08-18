package org.homio.api.model.device;

import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class ConfigDeviceDefinitions {
    // not uses for now
    private int version;
    private List<ConfigDeviceDefinition> devices;
    // full list of all possible endpoints that contains endpoint icon/color/etc...
    private List<ConfigDeviceEndpoint> endpoints;
    // set of endpoints hide from UI
    private Set<String> hiddenEndpoints;
    // set of endpoints that should be fully ignored
    private Set<String> ignoreEndpoints;
}
