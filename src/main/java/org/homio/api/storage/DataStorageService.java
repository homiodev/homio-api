package org.homio.api.storage;

import com.mongodb.client.model.Filters;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.homio.api.entity.widget.AggregationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataStorageService<T extends DataStorageEntity> {

    List<SourceHistoryItem> getSourceHistoryItems(@Nullable String field, @Nullable String value, int from, int count);

    default SourceHistory getSourceHistory(@Nullable String field, @Nullable String value) {
        return new SourceHistory(
                ((Number) aggregate(null, null, field, value, AggregationType.Count, false)).intValue(),
                ((Number) aggregate(null, null, field, value, AggregationType.Min, false)).floatValue(),
                ((Number) aggregate(null, null, field, value, AggregationType.Max, false)).floatValue(),
                ((Number) aggregate(null, null, field, value, AggregationType.Median, false)).floatValue()
        );
    }

    T save(@NotNull T entity);

    void save(@NotNull List<T> entities);

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

    default @NotNull List<T> findAllBy(@NotNull String field, @NotNull String value, @Nullable SortBy sort,
                                       @Nullable Integer limit) {
        return queryListWithSort(Filters.eq(field, value), sort, limit);
    }

    default @NotNull List<T> findAllBy(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, null, null);
    }

    default @NotNull List<T> findAllBySortAsc(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, SortBy.sortAsc(InMemoryDB.CREATED), null);
    }

    default @NotNull List<T> findAllBySortDesc(@NotNull String field, @NotNull String value) {
        return findAllBy(field, value, SortBy.sortDesc(InMemoryDB.CREATED), null);
    }

    @Nullable T findLatestBy(@NotNull String field, @NotNull String value);

    @Nullable T getLatest();

    default List<T> findAll(@Nullable SortBy sort, @Nullable Integer limit) {
        return queryListWithSort(new Document(), sort, limit);
    }

    @NotNull List<T> queryListWithSort(Bson filter, SortBy sort, Integer limit);

    default @NotNull List<T> findAllSince(long timestamp) {
        return queryListWithSort(Filters.gte(InMemoryDB.CREATED, timestamp), SortBy.sortAsc(InMemoryDB.CREATED), null);
    }

    default @NotNull List<T> findAll() {
        return findAll(null, null);
    }

    default @NotNull List<T> findByPattern(@NotNull String field, @NotNull String pattern, @Nullable SortBy sort,
                                           @Nullable Integer limit) {
        return queryListWithSort(Filters.eq(field, Pattern.compile(pattern)), sort, limit);
    }

    default @NotNull List<T> findByPattern(@NotNull String field, @NotNull String value) {
        return findByPattern(field, value, null, null);
    }

    @Nullable Long getQuota();

    default @NotNull List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to) {
        return getTimeSeries(from, to, null, null);
    }

    void updateQuota(@Nullable Long quota);

    long getUsed();

    default @NotNull List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field,
                                                  @Nullable String value) {
        return getTimeSeries(from, to, field, value, "value");
    }

    @NotNull List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                                          @NotNull String aggregateField);

    default @NotNull Object aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                                      @NotNull AggregationType aggregationType, boolean filterOnlyNumbers) {
        return aggregate(from, to, field, value, aggregationType, filterOnlyNumbers, "value");
    }

    @NotNull Object aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                              @NotNull AggregationType aggregationType, boolean filterOnlyNumbers,
                              @NotNull String aggregateField);

    @NotNull DataStorageService<T> addSaveListener(@NotNull String discriminator, @NotNull Consumer<T> listener);
}
