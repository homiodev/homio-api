package org.homio.api;

import java.awt.*;
import java.util.*;
import java.util.stream.Collectors;
import org.homio.api.model.OptionModel;
import org.homio.api.stream.video.VideoPlayer;
import org.jetbrains.annotations.NotNull;

public interface ContextMediaVideo {

  @NotNull
  Context context();

  Unregistered addVideoSource(@NotNull String path, @NotNull String source);

  Unregistered addVideoSourceListener(
      @NotNull String key, @NotNull RegisterVideoSourceListener listener);

  Unregistered addVideoSourceInfo(
      @NotNull String path, @NotNull Map<String, OptionModel> videoSources);

  Unregistered addVideoSourceInfoListener(
      @NotNull String name, @NotNull VideoSourceInfoListener listener);

  Unregistered addVideoWebRTCProvider(@NotNull String name, int port);

  Unregistered addVideoHLSProvider(@NotNull String name, int port);

  @NotNull
  VideoInputDevice createVideoInputDevice(@NotNull String vfile);

  Unregistered addVideoPlayer(@NotNull VideoPlayer player);

  /**
   * @return - Get usb camera
   */
  @NotNull
  Set<String> getVideoDevices();

  interface VideoInputDevice {

    @NotNull
    String getName();

    @NotNull
    VideoInputDevice setName(@NotNull String value);

    @NotNull
    Dimension[] getResolutions();

    default @NotNull Set<String> getResolutionSet() {
      Dimension[] resolutions = getResolutions();
      return Arrays.stream(resolutions)
          .sorted(Comparator.comparingInt(o -> o.width + o.height))
          .map(r -> String.format("%dx%d", r.width, r.height))
          .collect(Collectors.toCollection(LinkedHashSet::new));
    }
  }

  interface VideoSourceInfoListener {
    void addVideoSourceInfo(String path, Map<String, OptionModel> videoSources);

    void removeVideoSourceInfo(String path);
  }

  interface RegisterVideoSourceListener {
    void addVideoSource(@NotNull String path, @NotNull String source);

    void removeVideoSource(@NotNull String path);
  }
}
