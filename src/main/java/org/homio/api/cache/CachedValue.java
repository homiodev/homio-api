package org.homio.api.cache;

import java.time.Duration;
import java.util.function.Function;

/**
 * Store cached value. Fetch new value on ttl
 */
public class CachedValue<T, P> {

    private final Function<P, T> fetchHandler;
    private final Duration ttl;
    private long lastCheck = 0;
    private T value;

    public CachedValue(Duration ttl, Function<P, T> fetchHandler) {
        this.fetchHandler = fetchHandler;
        this.ttl = ttl;
    }

    public T getValue(P parameter) {
        if (System.currentTimeMillis() - lastCheck > ttl.toMillis()) {
            this.setValue(fetchHandler.apply(parameter));
        }
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
        this.lastCheck = System.currentTimeMillis();
    }
}
