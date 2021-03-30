package org.touchhome.bundle.api.entity;

import org.apache.commons.lang3.StringUtils;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.io.Serializable;

public interface BaseEntityIdentifier<T> extends HasEntityIdentifier, Serializable {

    default String getDefaultName() {
        return null;
    }

    default String getTitle() {
        return StringUtils.defaultIfBlank(getName(), StringUtils.defaultIfBlank(getDefaultName(), getEntityID()));
    }

    default String getType() {
        return this.getClass().getSimpleName();
    }

    String getName();

    default void afterDelete(EntityContext entityContext) {

    }

    default void afterUpdate(EntityContext entityContext) {

    }

    // fires after fetch from db/cache
    default void afterFetch(EntityContext entityContext) {

    }

    default String refreshName() {
        return null;
    }

    String getEntityPrefix();
}
