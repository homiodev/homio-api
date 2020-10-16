package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasEntityIdentifier {
    String getEntityID();

    @JsonIgnore
    Integer getId();

    @JsonIgnore
    default String getIdentifier() {
        return getId() == null ? null : String.valueOf(getId());
    }
}
