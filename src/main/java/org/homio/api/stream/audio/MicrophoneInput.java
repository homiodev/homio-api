package org.homio.api.stream.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Audio input
 */
public interface MicrophoneInput {

    @NotNull
    String getId();

    @NotNull
    Set<AudioFormat> getSupportedFormats();

    @Nullable
    AudioStream getInputStream(@NotNull AudioFormat format) throws Exception;

    default boolean isAvailable() {
        return true;
    }
}
