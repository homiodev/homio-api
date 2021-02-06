package org.touchhome.bundle.api.model;

public interface HasEntityIdentifier {
    String getEntityID();

    default String getIdentifier() {
        return getEntityID();
    }
}
