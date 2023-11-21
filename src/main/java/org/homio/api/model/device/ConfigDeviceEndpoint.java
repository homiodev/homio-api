package org.homio.api.model.device;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.json.JSONObject;

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
    private Boolean ignoreDuplicates; // default is true if null
    private Integer quota;
    private Float min;
    private Float max;
    private List<String> alias;
    private JSONObject metadata = new JSONObject();

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        metadata.put(key, value);
    }
}
