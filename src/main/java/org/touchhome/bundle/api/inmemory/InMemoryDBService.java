package org.touchhome.bundle.api.inmemory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static org.touchhome.bundle.api.inmemory.InMemoryDB.CREATED;

public interface InMemoryDBService<T extends InMemoryDBEntity> {

    T save(@NotNull T entity);

    default long count() {
        return count(null, null);
    }

    long count(@Nullable Long from, @Nullable Long to);

    default long delete(@NotNull T entity) {
        return deleteBy("_id", entity.getId());
    }

    long deleteBy(@NotNull String field, @NotNull Object value);

    default long deleteByPattern(@NotNull String field, @NotNull String prefix) {
        return deleteBy(field, Pattern.compile(prefix));
    }

    long deleteAll();

    List<T> findAllBy(@NotNull String field, @NotNull String value, @Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findAllBy(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, null, null);
    }

    default List<T> findAllBySortAsc(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, SortBy.sortAsc(CREATED), null);
    }

    default List<T> findAllBySortDesc(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, SortBy.sortDesc(CREATED), null);
    }

    T findLatestBy(@NotNull String field, @NotNull String value);

    T getLatest();

    List<T> findAll(@Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findAll() {
        return findAll(null, null);
    }

    List<T> findByPattern(@NotNull String field, @NotNull String value, @Nullable SortBy sort, @Nullable Integer limit);

    default List<T> findByPattern(@NotNull String field, @NotNull String value) {
        return findByPattern(field, value, null, null);
    }

    Long getQuota();

    default List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to) {
        return getTimeSeries(from, to, null, null);
    }

    void updateQuota(@Nullable Long quota);

    long getUsed();

    default List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value) {
        return getTimeSeries(from, to, field, value, "value");
    }

    List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                                 @NotNull String aggregateField);

    default Object aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                             @NotNull AggregationType aggregationType, boolean filterOnlyNumbers) {
        return aggregate(from, to, field, value, aggregationType, filterOnlyNumbers, "value");
    }

    Object aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                     @NotNull AggregationType aggregationType, boolean filterOnlyNumbers, @NotNull String aggregateField);

    InMemoryDBService<T> addSaveListener(String discriminator, Consumer<T> listener);
}
