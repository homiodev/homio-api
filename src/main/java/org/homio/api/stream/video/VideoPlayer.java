package org.homio.api.stream.video;

import org.homio.api.stream.StreamPlayer;

import java.util.Set;

public interface VideoPlayer extends StreamPlayer {
  default Set<VideoFormat> getVideoSupportedFormats() {
    return Set.of(VideoFormat.MP4, VideoFormat.AVI);
  }
}
