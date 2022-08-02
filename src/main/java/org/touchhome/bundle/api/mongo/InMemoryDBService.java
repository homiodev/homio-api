package org.touchhome.bundle.api.mongo;

import dev.morphia.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;

import java.util.List;
import java.util.stream.Stream;

public interface InMemoryDBService<T extends InMemoryDBEntity> {

    T save(T entity);

    default long count() {
        return count(null, null);
    }

    long count(@Nullable Long from, @Nullable Long to);

    long delete(T entity);

    long deleteBy(String field, String value);

    long deleteByPattern(String field, String prefix);

    long deleteAll();

    Stream<T> findBy(String field, String value);

    Stream<T> findAll();

    Stream<T> findByPattern(String field, String value);

    Query<T> find(T entity);

    Long getQuota();

    default List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to) {
        return getTimeSeries(from, to, null, null);
    }

    void updateQuota(Long quota);

    long getUsed();

    List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value);

    Float aggregate(Long from, Long to, @Nullable String field, @Nullable String value,
                    @NotNull AggregationType aggregationType);
}
