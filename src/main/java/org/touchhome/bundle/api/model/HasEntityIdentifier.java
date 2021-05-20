package org.touchhome.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasEntityIdentifier {
    String getEntityID();

    @JsonIgnore
    default String getIdentifier() {
        return getEntityID();
    }
}
