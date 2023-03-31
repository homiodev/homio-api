package org.homio.bundle.api.entity;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.model.HasEntityIdentifier;

public interface BaseEntityIdentifier<T> extends HasEntityIdentifier, Serializable {

    @JsonIgnore
    String getDefaultName();

    default String getTitle() {
        return defaultIfBlank(getName(), defaultIfBlank(getDefaultName(), getEntityID()));
    }

    default String getType() {
        return this.getClass().getSimpleName();
    }

    String getName();

    default void afterDelete(EntityContext entityContext) {

    }

    default void afterUpdate(EntityContext entityContext, boolean persis) {

    }

    // fires after fetch from db/cache
    default void afterFetch(EntityContext entityContext) {

    }

    @JsonIgnore
    default String refreshName() {
        return getDefaultName();
    }

    @JsonIgnore
    String getEntityPrefix();
}
