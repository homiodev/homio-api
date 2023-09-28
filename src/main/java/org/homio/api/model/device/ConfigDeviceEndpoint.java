package org.homio.api.model.device;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@NoArgsConstructor
public class ConfigDeviceEndpoint {

    private String name;
    private String icon;
    private String iconColor;
    private int order;
    private String unit;
    private boolean stateless;
    private boolean persistent;
    private Integer quota;
    private Float min;
    private Float max;
    private List<String> alias;
}
