package org.homio.api.stream.audio;

import org.homio.api.stream.ContentStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Audio input
 */
public interface AudioInput {

    @NotNull
    String getId();

    @NotNull
    Set<AudioFormat> getSupportedFormats();

    @Nullable
    ContentStream getResource() throws Exception;

    default boolean isAvailable() {
        return true;
    }
}
