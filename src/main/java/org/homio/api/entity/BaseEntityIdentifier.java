package org.homio.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.homio.api.EntityContext;
import org.homio.api.model.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public interface BaseEntityIdentifier extends EntityFieldMetadata, Serializable {

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

    /**
     * Specify entity font awesome icon for UI purposes
     */
    default @Nullable Icon getEntityIcon() {
        return null;
    }

    /**
     * Specify type for dynamic update. For widget it will be always: 'widget', etc...
     *
     * @return - dynamic widget type
     */
    default @NotNull String getDynamicUpdateType() {
        return getType();
    }
}
