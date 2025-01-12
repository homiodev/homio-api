package org.homio.api.cache;

import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingSupplier;
import lombok.SneakyThrows;

import java.time.Duration;

/**
 * Store cached value. Fetch new value on ttl
 */
public class CachedValue<T, P> {

  private final ThrowingFunction<P, T, Exception> fetchHandler;
  private final Duration ttl;
  private long lastCheck = 0;
  private T value;

  public CachedValue(Duration ttl, ThrowingFunction<P, T, Exception> fetchHandler) {
    this.fetchHandler = fetchHandler;
    this.ttl = ttl;
  }

  public CachedValue(Duration ttl, ThrowingSupplier<T, Exception> fetchHandler) {
    this.fetchHandler = arg -> fetchHandler.get();
    this.ttl = ttl;
  }

  @SneakyThrows
  public T getValue(P parameter) {
    if (System.currentTimeMillis() - lastCheck > ttl.toMillis()) {
      this.setValue(fetchHandler.apply(parameter));
    }
    return this.value;
  }

  @SneakyThrows
  public T getValue() {
    if (System.currentTimeMillis() - lastCheck > ttl.toMillis()) {
      this.setValue(fetchHandler.apply(null));
    }
    return this.value;
  }

  public void setValue(T value) {
    this.value = value;
    this.lastCheck = System.currentTimeMillis();
  }
}
