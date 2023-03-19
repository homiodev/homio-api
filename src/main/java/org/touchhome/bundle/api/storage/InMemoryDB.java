package org.touchhome.bundle.api.storage;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import lombok.RequiredArgsConstructor;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;

import java.net.InetSocketAddress;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;
import static org.bson.codecs.configuration.CodecRegistries.*;

public final class InMemoryDB {
    public static final String ID = "_id";
    public static final String CREATED = "created";
    private static final String DATABASE = "db";
    private static final Map<String, InMemoryDBDataService<?>> map = new ConcurrentHashMap<>();

    private static final MongoServer server;

    private static final MongoClient client;

    private static final MongoDatabase datastore;

    static {
        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        CodecRegistry pojoProvidersRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry pojoCodecRegistry = fromCodecs(new ObjectCodec());
        CodecRegistry codecRegistry =
                fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoProvidersRegistry, pojoCodecRegistry);

        client = MongoClients.create(MongoClientSettings.builder().codecRegistry(codecRegistry).applyToClusterSettings(
                builder -> builder.hosts(Collections.singletonList(new ServerAddress(serverAddress)))).build());
        datastore = client.getDatabase(DATABASE);
        /*Mapper mapper = new Mapper(datastore, datastore.getDatabase().getCodecRegistry(), MapperOptions.DEFAULT) {
            @Override
            public <T> boolean isMappable(Class<T> type) {
                return super.isMappable(type) || InMemoryDBEntity.class.isAssignableFrom(type);
            }
        };
        try {
            FieldUtils.writeDeclaredField(datastore, "mapper", mapper, true);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }*/

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });
    }

    public static <T extends DataStorageEntity> DataStorageService<T> getOrCreateService(
            @NotNull Class<T> pojoClass, @Nullable Long quota) {
        return InMemoryDB.getOrCreateService(pojoClass, pojoClass.getSimpleName(), quota);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataStorageEntity> DataStorageService<T> getOrCreateService(@NotNull Class<T> pojoClass,
                                                                                         @NotNull String uniqueId,
                                                                                         @Nullable Long quota) {
        return (DataStorageService<T>) map.computeIfAbsent(uniqueId, aClass -> {
            String collectionName = pojoClass.getSimpleName() + uniqueId;
            // create timestamp index
            MongoCollection<T> collection = datastore.getCollection(collectionName, pojoClass);
            //          collection.createIndex(Indexes.ascending(ID));
            //          collection.createIndex(Indexes.ascending("topic"));

            // delta is 10% of quota but not more than 1000
            InMemoryDBDataService<T> data = new InMemoryDBDataService<>(pojoClass, collectionName, collection);
            data.updateQuota(quota);
            return data;
        });
    }

    /**
     * Remove service from map and clean all data
     */
    public static <T extends DataStorageEntity> DataStorageService<T> removeService(String uniqueId) {
        DataStorageService<T> service = (DataStorageService<T>) map.remove(uniqueId);
        if (service != null) {
            service.deleteAll();
        }
        return service;
    }

    public static Number toNumber(Object value) {
        if (value == null) return 0;
        if (Number.class.isAssignableFrom(value.getClass())) {
            return ((Number) value);
        }
        String vStr = String.valueOf(value);
        if ("false".equals(vStr)) return 0;
        try {
            return NumberFormat.getInstance().parse(vStr).floatValue();
        } catch (Exception ignored) {
        }
        return vStr.isEmpty() ? 0 : 1;
    }

    /*public static void main(String[] args) throws InterruptedException {
        long start0 = System.currentTimeMillis();
        InMemoryDBService<Test> service = InMemoryDB.getOrCreateService(Test.class, 10000L);
        for (int i = 0; i < 10000; i++) {
            service.save(new Test("$SYS/data_" + i % 2, i));
            Thread.sleep(1);
        }

        long start = System.currentTimeMillis();
        service.getTimeSeries(1111110L, null, "topic", "$SYS/data_1");

        System.out.println((System.currentTimeMillis() - start));
        System.out.println((System.currentTimeMillis() - start0));
    }

    @Getter
    @Setter
    @ToString(callSuper = true)
    @NoArgsConstructor
    public static class Test extends InMemoryDBEntity {
        private String topic;

        public Test(String topic, Object value) {
            super(value);
            this.topic = topic;
        }
    }*/

    @RequiredArgsConstructor
    private static class InMemoryDBDataService<T extends DataStorageEntity> implements DataStorageService<T> {

        private final Class<T> pojoClass;
        private final String collectionName;
        private final MongoCollection<T> collection;
        private final AtomicLong estimateUsed = new AtomicLong(0);
        private final Map<String, Consumer<T>> saveListeners = new HashMap<>();
        private Long quota;
        private int delta;

        @Override
        public List<SourceHistoryItem> getSourceHistoryItems(@Nullable String field, @Nullable String value, int from,
                                                             int count) {
            Bson filter = field == null || value == null ? new Document() : Filters.eq(field, value);
            try (MongoCursor<T> cursor = queryWithSort(filter, SortBy.sortDesc(CREATED), count, from)) {
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cursor, 0), false)
                        .map(t -> new SourceHistoryItem(t.getCreated(), t.getValue()))
                        .collect(Collectors.toList());
            }
        }

        @Override
        public void save(@NotNull List<T> entities) {
            collection.insertMany(entities);
            postInsertQuotaHandler();
        }

        @Override
        public T save(@NotNull T entity) {
            collection.insertOne(entity);
            for (Consumer<T> listener : saveListeners.values()) {
                listener.accept(entity);
            }
            postInsertQuotaHandler();
            return entity;
        }

        private void postInsertQuotaHandler() {
            if (quota != null) {
                estimateUsed.incrementAndGet();

                if (estimateUsed.get() > quota) {
                    synchronized (this) {
                        estimateUsed.set(count()); // calc precise amount of saved documents

                        if (estimateUsed.get() > quota) {
                            List<Long> itemsToRemove;
                            try (MongoCursor<Document> cursor = collection.aggregate(Arrays.asList(
                                    Aggregates.sort(ascending(CREATED)),
                                    Aggregates.limit(delta),
                                    Aggregates.project(Projections.include("_id")),
                                    Aggregates.group("ids", Accumulators.addToSet("ids", "$_id"))
                            ), Document.class).cursor()) {
                                itemsToRemove = (List<Long>) cursor.next().get("ids", List.class);
                            }
                            updateUsed(-collection.deleteMany(Filters.in("_id", itemsToRemove)).getDeletedCount());
                        }
                    }
                }
            }
        }

        @Override
        public long count(Long from, Long to) {
            if (from != null || to != null) {
                return collection.countDocuments(buildCreatedFilter(from, to));
            }
            return collection.countDocuments();
        }

        @Override
        public long deleteBy(@NotNull String field, @NotNull Object value) {
            return -updateUsed(-collection.deleteMany(Filters.eq(field, value)).getDeletedCount());
        }

        @Override
        public long deleteAll() {
            return -updateUsed(-collection.deleteMany(new Document()).getDeletedCount());
        }

        @Override
        public T findLatestBy(@NotNull String field, @NotNull String value) {
            try (MongoCursor<T> cursor = queryWithSort(Filters.eq(field, value), SortBy.sortDesc(CREATED), 1, null)) {
                return cursor.tryNext();
            }
        }

        @Override
        public T getLatest() {
            try (MongoCursor<T> cursor = queryWithSort(new Document(), SortBy.sortDesc(CREATED), 1, null)) {
                return cursor.tryNext();
            }
        }

        @Override
        public Long getQuota() {
            return quota;
        }

        @Override
        public void updateQuota(@Nullable Long quota) {
            if (quota == null || quota == 0) {
                this.quota = null;
            } else {
                if (!Objects.equals(quota, this.quota)) {
                    this.quota = quota;
                    int delta = (int) (quota * 10 / 100);
                    this.delta = Math.min(delta, Math.max(delta, 10));
                }
            }
        }

        @Override
        public long getUsed() {
            // refresh estimateUsed on each request
            estimateUsed.set(count());
            return estimateUsed.get();
        }

        @Override
        public List<Object[]> getTimeSeries(@Nullable Long from, @Nullable Long to, @Nullable String field,
                                            @Nullable String value, @NotNull String aggregateField) {
            List<Bson> filterList = buildBsonFilter(from, to, field, value);

            try (MongoCursor<Document> cursor = collection.aggregate(Arrays.asList(
                    Aggregates.match(joinFilters(filterList)),
                    Aggregates.sort(ascending(CREATED)),
                    Aggregates.project(Projections.include(CREATED, aggregateField))), Document.class).cursor()) {
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cursor, 0), false)
                        .map(doc -> new Object[]{doc.get(CREATED), toNumber(doc.get(aggregateField)).floatValue()})
                        .collect(Collectors.toList());
            }
        }

        @Override
        public @NotNull List<T> queryListWithSort(Bson filter, SortBy sort, Integer limit) {
            try (MongoCursor<T> cursor = queryWithSort(filter, sort, limit, null)) {
                return StreamSupport.stream(Spliterators.spliteratorUnknownSize(cursor, 0), false)
                        .collect(Collectors.toList());
            }
        }

        @Override
        public Object aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                                @NotNull AggregationType aggregationType, boolean filterOnlyNumbers,
                                @NotNull String aggregateField) {
            List<Bson> filterList = buildBsonFilter(from, to, field, value);
            Bson bsonFilter = joinFilters(filterList);
            switch (aggregationType) {
                case First:
                    return aggregateMinimal(aggregateField, bsonFilter, SortBy.sortAsc(CREATED));
                case Last:
                    return aggregateMinimal(aggregateField, bsonFilter, SortBy.sortDesc(CREATED));
                case Min:
                    return aggregateMinimal(aggregateField, bsonFilter, SortBy.sortAsc(aggregateField));
                case Max:
                    return aggregateMinimal(aggregateField, bsonFilter, SortBy.sortDesc(aggregateField));
                case Count:
                    return collection.countDocuments(bsonFilter);
            }

            if (filterOnlyNumbers) {
                filterList.add(Filters.or(
                        Filters.type(aggregateField, BsonType.DOUBLE),
                        Filters.type(aggregateField, BsonType.INT64),
                        Filters.type(aggregateField, BsonType.INT32)
                ));
            }

            List<Bson> pipeline = new ArrayList<>();
            bsonFilter = joinFilters(filterList);
            if (!filterList.isEmpty()) {
                pipeline.add(Aggregates.match(joinFilters(filterList)));
            }

            switch (aggregationType) {
                case Average:
                case AverageNoZero:
                    pipeline.add(Aggregates.group("_id", Accumulators.avg(aggregateField, "$" + aggregateField)));
                    break;
                case Sum:
                    pipeline.add(Aggregates.group("_id", Accumulators.sum(aggregateField, "$" + aggregateField)));
                    break;
                case Median:
                    long count = collection.countDocuments(bsonFilter);
                    pipeline.add(Aggregates.sort(ascending(aggregateField)));
                    if (count % 2 == 0) {
                        pipeline.add(Aggregates.skip((int) (count / 2 - 1)));
                        pipeline.add(Aggregates.limit(2));
                        pipeline.add(Aggregates.group("_id", Accumulators.avg(aggregateField, "$" + aggregateField)));
                    } else {
                        pipeline.add(Aggregates.skip((int) (count / 2)));
                        pipeline.add(Aggregates.limit(1));
                    }
                    break;
            }

            try (MongoCursor<Document> cursor = collection.aggregate(pipeline, Document.class).cursor()) {
                Document document = cursor.tryNext();
                return document == null ? null : document.get(aggregateField);
            }
        }

        private Bson joinFilters(List<Bson> filterList) {
            return filterList.isEmpty() ? new Document() :
                    filterList.size() == 1 ? filterList.iterator().next() : Filters.and(filterList);
        }

        @Override
        public InMemoryDBDataService<T> addSaveListener(String discriminator, Consumer<T> listener) {
            this.saveListeners.put(discriminator, listener);
            return this;
        }

        private Bson buildCreatedFilter(Long from, Long to) {
            Bson filter = null;
            if (from != null && to != null) {
                filter = Filters.and(Filters.gte(CREATED, from), Filters.lte(CREATED, to));
            } else if (from != null) {
                filter = Filters.gte(CREATED, from);
            } else if (to != null) {
                filter = Filters.lte(CREATED, to);
            }
            return filter;
        }

        private List<Bson> buildBsonFilter(Long from, Long to, String field, String value) {
            List<Bson> filters = new ArrayList<>();
            if (from != null) {
                filters.add(com.mongodb.client.model.Filters.gte(CREATED, from));
            }
            if (to != null) {
                filters.add(com.mongodb.client.model.Filters.lte(CREATED, to));
            }
            if (field != null && value != null) {
                filters.add(com.mongodb.client.model.Filters.eq(field, value));
            }
            return filters;
        }

        private long updateUsed(long changed) {
            estimateUsed.addAndGet(changed);
            return changed;
        }

        private MongoCursor<T> queryWithSort(Bson query, SortBy sort, Integer limit, Integer skip) {
            FindIterable<T> ts = collection.find(query);
            if (sort != null) {
                ts.sort(sort.isAsc() ? ascending(sort.getOrderField()) : descending(sort.getOrderField()));
                if (limit != null) {
                    ts.limit(limit);
                }
                if (skip != null) {
                    ts.skip(skip);
                }
            }
            return ts.cursor();
        }

        private Object aggregateMinimal(@NotNull String aggregateField, Bson filter, SortBy sort) {
            Document document = collection.find(filter, Document.class).sort(sort.toBson()).limit(1).first();
            return document == null ? null : document.get(aggregateField);
        }
    }
}