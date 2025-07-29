package org.homio.api.stream;

import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

// fires when timeout is reached
public interface ContentStream extends Closeable {

  @NotNull
  Resource getResource();

  @NotNull
  StreamFormat getStreamFormat();

  @Override
  default void close() throws IOException {
    // do nothing
  }
}
