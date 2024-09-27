package org.homio.api.stream.audio;

import org.homio.api.stream.StreamPlayer;

import java.util.Set;

public interface AudioPlayer extends StreamPlayer {

    default Set<AudioFormat> getAudioSupportedFormats() {
        return Set.of(AudioFormat.MP3, AudioFormat.WAV);
    }
}
