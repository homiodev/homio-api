package org.touchhome.bundle.api.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import dev.morphia.Morphia;
import dev.morphia.aggregation.experimental.Aggregation;
import dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.experimental.stages.Group;
import dev.morphia.aggregation.experimental.stages.Projection;
import dev.morphia.aggregation.experimental.stages.Sort;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.Query;
import dev.morphia.query.Type;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import lombok.RequiredArgsConstructor;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.entity.widget.AggregationType;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions.addToSet;
import static dev.morphia.aggregation.experimental.expressions.Expressions.field;
import static dev.morphia.aggregation.experimental.stages.Group.group;
import static dev.morphia.aggregation.experimental.stages.Group.id;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public final class InMemoryDB {
    private static final String DATABASE = "db";
    private static final Map<Class<?>, InMemoryDBData<?>> map = new HashMap<>();

    private static final Datastore datastore;

    private static final MongoServer server;

    private static final MongoClient client;

    static {
        server = new MongoServer(new MemoryBackend());

        // bind on a random local port
        InetSocketAddress serverAddress = server.bind();

        CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);

        client = MongoClients.create(MongoClientSettings.builder().codecRegistry(codecRegistry).applyToClusterSettings(
                builder -> builder.hosts(Collections.singletonList(new ServerAddress(serverAddress)))).build());
        datastore = Morphia.createDatastore(client, DATABASE);
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

    @SuppressWarnings("unchecked")
    public static <T extends InMemoryDBEntity> InMemoryDBService<T> getService(@NotNull Class<T> pojoClass,
                                                                               @Nullable Long quota) {
        return (InMemoryDBService<T>) map.computeIfAbsent(pojoClass, aClass -> {
            String collectionName = pojoClass.getSimpleName();
            EntityBuilder entityBuilder = new EntityBuilder().useCollection(collectionName);
            datastore.getMapper().mapExternal(entityBuilder, pojoClass);

            // create timestamp index
            /*MongoCollection<T> collection = datastore.getMapper().getCollection(pojoClass);
            org.bson.Document keys = new org.bson.Document();
            keys.putAll(new org.bson.Document("created", IndexType.ASC.toString()));
            IndexOptions indexOptions = new IndexOptions();
            collection.createIndex(keys, indexOptions);*/
            datastore.ensureIndexes(pojoClass);

            // delta is 10% of quota but not more than 1000
            InMemoryDBData<T> data = new InMemoryDBData<>(pojoClass, collectionName);
            data.updateQuota(quota);
            return data;
        });
    }

    @RequiredArgsConstructor
    private static class InMemoryDBData<T extends InMemoryDBEntity> implements InMemoryDBService<T> {

        private final Class<T> pojoClass;
        private final String collectionName;
        private final AtomicLong estimateUsed = new AtomicLong(0);
        private Long quota;
        private int delta;

        @Override
        public T save(T entity) {
            entity.updated = System.currentTimeMillis();

            T saved = datastore.save(entity);
            if (quota != null) {
                estimateUsed.incrementAndGet();

                if (estimateUsed.get() > quota) {
                    synchronized (this) {
                        estimateUsed.set(count()); // calc precise amount of saved documents

                        if (estimateUsed.get() > quota) {

                            List<String> itemsToRemove;
                            // aggregate is faster than .stream()...
                            try (MorphiaCursor<HashMap> execute = datastore.aggregate(pojoClass)
                                    .sort(Sort.sort().ascending("updated"))
                                    .limit(delta)
                                    .project(Projection.project().include("_id"))
                                    .group(Group.group().field("ids", addToSet(field("_id"))))
                                    .execute(HashMap.class)) {
                                //noinspection unchecked
                                itemsToRemove = (List<String>) execute.next().get("ids");
                            }

                            updateUsed(-getQuery().filter(Filters.in("_id", itemsToRemove))
                                    .delete(new DeleteOptions().multi(true)).getDeletedCount());
                        }
                    }
                }
            }
            return saved;
        }

        @Override
        public long count(Long from, Long to) {
            Query<T> query = getQuery();
            if (from != null || to != null) {
                query.filter(buildCreatedFilter(from, to));
            }
            return query.count();
        }

        @Override
        public long delete(T entity) {
            return -updateUsed(-datastore.delete(entity).getDeletedCount());
        }

        @Override
        public long deleteBy(String field, String value) {
            return -updateUsed(
                    -getQuery().filter(Filters.eq(field, value)).delete(new DeleteOptions().multi(true)).getDeletedCount());
        }

        @Override
        public long deleteByPattern(String field, String pattern) {
            return -updateUsed(-getQuery().
                    filter(Filters.eq(field, Pattern.compile(pattern)))
                    .delete(new DeleteOptions().multi(true)).getDeletedCount());
        }

        @Override
        public long deleteAll() {
            return -updateUsed(-getQuery().delete(new DeleteOptions().multi(true)).getDeletedCount());
        }

        @Override
        public Stream<T> findBy(String field, String value) {
            return getQuery().filter(Filters.eq(field, value)).stream();
        }

        @Override
        public Stream<T> findAll() {
            return getQuery().stream();
        }

        @Override
        public Stream<T> findByPattern(String field, String pattern) {
            return datastore.find(pojoClass).filter(Filters.eq(field, Pattern.compile(pattern))).stream();
        }

        @Override
        public Query<T> find(T entity) {
            return getQuery();
        }

        @Override
        public Long getQuota() {
            return quota;
        }

        @Override
        public void updateQuota(Long quota) {
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
                                            @Nullable String value) {
            List<Object[]> list;
            List<Filter> filters = buildAggregateFilter(from, to, field, value);

            try (MorphiaCursor<Map> cursor = datastore.aggregate(pojoClass)
                    .match(Filters.and(filters.toArray(Filter[]::new)))
                    .project(Projection.project().include("updated").include("value"))
                    .execute(Map.class)) {

                list = new ArrayList<>();
                while (cursor.hasNext()) {
                    Map document = cursor.next();
                    list.add(new Object[]{document.get("updated"), ((Number)document.get("value")).floatValue()});
                }
            }
            return list;
        }

        @Override
        public Float aggregate(@Nullable Long from, @Nullable Long to, @Nullable String field, @Nullable String value,
                               @NotNull AggregationType aggregationType) {
            List<Filter> filters = buildAggregateFilter(from, to, field, value);

            Aggregation<T> aggregation = datastore.aggregate(pojoClass)
                    .match(Filters.and(filters.toArray(Filter[]::new)));
            switch (aggregationType) {
                case First:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.first(field("value"))));
                    break;
                case Last:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.last(field("value"))));
                    break;
                case Min:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.min(field("value"))));
                    break;
                case Max:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.max(field("value"))));
                    break;
                case Sum:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.sum(field("value"))));
                    break;
                case Count:
                    return (float) datastore.find(collectionName, pojoClass).filter(filters.toArray(Filter[]::new)).count();
                case Average:
                    aggregation.group(group(id()).field("value", AccumulatorExpressions.avg(field("value"))));
                    break;
                case Median:
                    long count = datastore.find(collectionName, pojoClass).filter(filters.toArray(Filter[]::new)).count();
                    aggregation.sort(Sort.sort().ascending("value"));
                    if (count % 2 == 0) {
                        aggregation.skip(count / 2 - 1).limit(2).group(group(id())
                                .field("value", AccumulatorExpressions.avg(field("value"))));
                    } else {
                        aggregation.skip(count / 2).limit(1);
                    }
                    break;
            }

            try (MorphiaCursor<Map> cursor = aggregation.execute(Map.class)) {
                Map document = cursor.tryNext();
                return document == null ? null : ((Number) document.get("value")).floatValue();
            }
        }

        private Query<T> getQuery() {
            return datastore.find(collectionName, pojoClass);
        }

        private Filter buildCreatedFilter(Long from, Long to) {
            Filter filter = null;
            if (from != null && to != null) {
                filter = Filters.and(
                        Filters.gte("updated", from),
                        Filters.lte("updated", to));
            } else if (from != null) {
                filter = Filters.gte("updated", from);
            } else if (to != null) {
                filter = Filters.lte("updated", to);
            }
            return filter;
        }

        private List<Filter> buildAggregateFilter(@Nullable Long from, @Nullable Long to, @Nullable String field,
                                                  @Nullable String value) {
            List<Filter> filters = new ArrayList<>();
            filters.add(Filters.or(
                    new TypeFilter("value", Type.INTEGER_32_BIT),
                    new TypeFilter("value", Type.INTEGER_64_BIT),
                    new TypeFilter("value", Type.DOUBLE)));
            if (from != null) {
                filters.add(Filters.gte("updated", from));
            }
            if (to != null) {
                filters.add(Filters.lte("updated", to));
            }
            if (field != null && value != null) {
                filters.add(Filters.eq(field, value));
            }
            return filters;
        }

        private long updateUsed(long changed) {
            estimateUsed.addAndGet(changed);
            return changed;
        }
    }

    /*public static void main(String args[]) {
        long start = System.currentTimeMillis();
        InMemoryDBService<Test> service = InMemoryDB.getService(Test.class, 10000L);
        for (int i = 0; i < 1_000_00; i++) {
            service.save(new Test("$SYS/data_" + i, 12));
        }

        System.out.println((System.currentTimeMillis() - start) / 1000);
    }

    @Getter
    public static class Test extends InMemoryDBEntity {
        private String topic;

        public Test(String topic, Object value) {
            super(Integer.toString(topic.hashCode()), value);
            this.topic = topic;
        }
    }*/
}
