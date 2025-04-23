package org.homio.api.stream.audio;

import java.util.Set;
import org.homio.api.stream.StreamPlayer;

public interface AudioPlayer extends StreamPlayer {

  default Set<AudioFormat> getAudioSupportedFormats() {
    return Set.of(AudioFormat.MP3, AudioFormat.WAV);
  }
}
