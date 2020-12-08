package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONPropertyIgnore;

public interface HasEntityIdentifier {
    String getEntityID();

    @JsonIgnore
    @JSONPropertyIgnore
    Integer getId();

    @JsonIgnore
    @JSONPropertyIgnore
    default String getIdentifier() {
        return getId() == null ? null : String.valueOf(getId());
    }
}
