package org.touchhome.bundle.api.repository;

import org.touchhome.bundle.api.model.HasEntityIdentifier;

public interface PureRepository<T extends HasEntityIdentifier> {
    void flushCashedEntity(T entity);

    Class<T> getEntityClass();
}
