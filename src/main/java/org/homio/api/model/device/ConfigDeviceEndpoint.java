package org.homio.api.model.device;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfigDeviceEndpoint {
    private String name;
    private String icon;
    private String iconColor;
    private int order;
    private String unit;
    private List<String> alias;
}
