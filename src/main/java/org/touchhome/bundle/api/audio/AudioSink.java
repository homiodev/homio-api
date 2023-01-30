package org.touchhome.bundle.api.audio;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface AudioSink {
    String getId();

    Map<String, String> getSources();

    String getLabel(@Nullable Locale locale);

    void play(AudioStream audioStream, String sinkSource, Integer startFrame, Integer endFrame) throws Exception;

    default int pause() {
        throw new IllegalStateException("Pause action is not implemented");
    }

    default void resume() {
        throw new IllegalStateException("Resume action is not implemented");
    }

    Set<AudioFormat> getSupportedFormats();

    Set<Class<? extends AudioStream>> getSupportedStreams();

    int getVolume() throws IOException;

    void setVolume(int volume) throws IOException;
}
