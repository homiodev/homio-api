package org.homio.api.stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;

import java.io.Closeable;

// fires when timeout is reached
public interface ContentStream extends Closeable {

    @NotNull
    Resource getResource();

    @Nullable
    String getMimeType();
}
