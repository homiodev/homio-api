package org.homio.api.stream;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

import java.io.Closeable;
import java.io.IOException;

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
