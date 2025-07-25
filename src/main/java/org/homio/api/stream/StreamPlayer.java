package org.homio.api.stream;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StreamPlayer {
    String getId();

    default @NotNull String getLabel() {
        return "TITLE." + getId();
    }

    void play(@NotNull ContentStream stream, @Nullable Integer startFrame, @Nullable Integer endFrame) throws Exception;

    default boolean isPlaying() {
        return false;
    }

    default void pause() {
        stop();
    }

    default void resume() {
        throw new IllegalStateException("Resume action is not implemented");
    }

    default void stop() {
        throw new IllegalStateException("Resume action is not implemented");
    }

    int getVolume() throws IOException;

    void setVolume(int volume) throws IOException;

    default boolean isAvailable() {
        return true;
    }
}
