package org.homio.bundle.api.repository;

import org.homio.bundle.api.model.HasEntityIdentifier;

public interface PureRepository<T extends HasEntityIdentifier> {
    void flushCashedEntity(T entity);

    Class<T> getEntityClass();
}
