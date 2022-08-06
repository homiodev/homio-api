package org.touchhome.bundle.api.inmemory;

import dev.morphia.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;

import java.util.List;

public interface InMemoryDBService<T extends InMemoryDBEntity> {

    T save(@NotNull T entity);

    default long count() {
        return count(null, null);
    }

    long count(@Nullable Long from, @Nullable Long to);

    long delete(@NotNull T entity);

    long deleteBy(@NotNull String field, @NotNull String value);

    long deleteByPattern(@NotNull String field, @NotNull String prefix);

    long deleteAll();

    List<T> findAllBy(@NotNull String field, @NotNull String value, @Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findAllBy(@NotNull String field, @NotNull String value) {
        return findAllBy(field, field, null, null);
    }

    T findLatestBy(@NotNull String field, @NotNull String value);

    List<T> findAll(@Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findAll() {
        return findAll(null, null);
    }

    List<T> findByPattern(@NotNull String field, @NotNull String value, @Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findByPattern(@NotNull String field, @NotNull String value) {
        return findByPattern(field, value, null, null);
    }

    Query<T> find(@NotNull T entity);

    Long getQuota();

    default List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to) {
        return getTimeSeries(from, to, null, null);
    }

    void updateQuota(@Nullable Long quota);

    long getUsed();

    List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value);

    Float aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                    @NotNull AggregationType aggregationType);
}
