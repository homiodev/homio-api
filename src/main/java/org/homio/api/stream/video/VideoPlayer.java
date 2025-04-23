package org.homio.api.stream.video;

import java.util.Set;
import org.homio.api.stream.StreamPlayer;

public interface VideoPlayer extends StreamPlayer {
  default Set<VideoFormat> getVideoSupportedFormats() {
    return Set.of(VideoFormat.MP4, VideoFormat.AVI);
  }
}
