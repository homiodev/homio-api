package org.homio.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface HasEntityIdentifier {

    @NotNull String getEntityID();

    default @Nullable String getTitle() {
        return getEntityID();
    }

    default @NotNull String getType() {
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    default @Nullable String getIdentifier() {
        return getEntityID();
    }
}
