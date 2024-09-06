package org.homio.api.stream.audio;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;

/**
 * Audio output
 */
public interface AudioSpeaker {

    String getId();

    default @NotNull String getLabel() {
        return "TITLE." + getId();
    }

    void play(@NotNull AudioStream audioStream, @Nullable Integer startFrame, @Nullable Integer endFrame) throws Exception;

    default void pause() {
        throw new IllegalStateException("Pause action is not implemented");
    }

    default void resume() {
        throw new IllegalStateException("Resume action is not implemented");
    }

    default void stop() {
        throw new IllegalStateException("Resume action is not implemented");
    }

    default Set<AudioFormat> getSupportedFormats() {
        return Set.of(AudioFormat.MP3, AudioFormat.WAV);
    }

    int getVolume() throws IOException;

    void setVolume(int volume) throws IOException;

    default boolean isAvailable() {
        return true;
    }
}
