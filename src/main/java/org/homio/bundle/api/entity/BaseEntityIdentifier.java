package org.homio.bundle.api.entity;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.homio.bundle.api.EntityContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BaseEntityIdentifier<T> extends EntityFieldMetadata, Serializable {

    @JsonIgnore
    String getDefaultName();

    default @NotNull String getTitle() {
        return defaultIfBlank(getName(), defaultIfBlank(getDefaultName(), getEntityID()));
    }

    @Nullable String getName();

    default void afterDelete(@NotNull EntityContext entityContext) {

    }

    default void afterUpdate(@NotNull EntityContext entityContext, boolean persis) {

    }

    // fires after fetch from db/cache
    default void afterFetch(@NotNull EntityContext entityContext) {

    }

    @JsonIgnore
    default @Nullable String refreshName() {
        return getDefaultName();
    }

    @JsonIgnore
    @NotNull String getEntityPrefix();
}
