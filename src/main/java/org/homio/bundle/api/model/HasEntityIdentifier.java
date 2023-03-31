package org.homio.bundle.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasEntityIdentifier {
    String getEntityID();

    default String getTitle() {
        return getEntityID();
    }

    @JsonIgnore
    default String getIdentifier() {
        return getEntityID();
    }
}
