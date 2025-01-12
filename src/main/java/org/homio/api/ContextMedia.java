package org.homio.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingRunnable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.Level;
import org.homio.api.model.Icon;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.audio.AudioInput;
import org.homio.api.stream.audio.AudioPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ContextMedia {

  @NotNull
  Context context();

  @NotNull
  ContextMediaVideo video();

  boolean isWebDriverAvailable();

  void fireSelenium(@NotNull ThrowingConsumer<WebDriver, Exception> driverHandler);

  // run with render content on UI
  void fireSelenium(@NotNull String title, @NotNull String icon, @NotNull String iconColor, @NotNull ThrowingConsumer<WebDriver, Exception> driverHandler);

  void fireFfmpeg(@NotNull String inputOptions, @NotNull String source, @NotNull String output, int maxWaitTimeout);

  @NonNull
  String getFfmpegLocation();

  void addAudioPlayer(@NotNull AudioPlayer player);

  void removeAudioPlayer(@NotNull AudioPlayer player);

  void addAudioInput(@NotNull AudioInput input);

  void removeAudioInput(@NotNull AudioInput input);

  /**
   * Create relative url .../stream to fetch data
   */
  @NotNull
  String createStreamUrl(@NotNull ContentStream stream, @Nullable Duration ttl);

  /**
   * @return - Get audio devices
   */
  @NotNull
  Set<String> getAudioDevices();

  @NotNull
  FFMPEG buildFFMPEG(@NotNull String entityID,
                     @NotNull String description,
                     @NotNull FFMPEGHandler handler,
                     @NotNull FFMPEGFormat format,
                     @NotNull String inputArguments,
                     @NotNull String input,
                     @NotNull String outArguments,
                     @NotNull String output,
                     @NotNull String username,
                     @NotNull String password);

  @Getter
  @RequiredArgsConstructor
  enum FFMPEGFormat {
    HLS("fas fa-square-rss", "#A62D79"),
    GIF("fas fa-images", "#3B8C8B"),
    RECORD("fas fa-microphone", "#B04B3E"),
    RTSP_ALARMS("fas fa-bell", "#8A29AB"),
    MJPEG("fas fa-photo-film", "#7FAEAA"),
    SNAPSHOT("fas fa-camera", "#A2D154"),
    RE("fas fa-kip-sign", "#3AB2BA"),
    DASH("fas fa-panorama", "#91A63C"),
    CUSTOM("fas fa-tower-cell", "#57A4D1");

    private final String icon;
    private final String color;

    public @NotNull Icon getIconModel() {
      return new Icon(icon, color);
    }
  }

  interface FFMPEG {

    @SneakyThrows
    static void run(@Nullable FFMPEG ffmpeg, @NotNull ThrowingConsumer<FFMPEG, Exception> handler) {
      if (ffmpeg != null) {
        handler.accept(ffmpeg);
      }
    }

    @SneakyThrows
    static <T> T execute(@Nullable FFMPEG ffmpeg, @NotNull ThrowingFunction<FFMPEG, T, Exception> handler) {
      if (ffmpeg != null) {
        return handler.apply(ffmpeg);
      }
      return null;
    }

    @SneakyThrows
    static <T> T check(
      @Nullable FFMPEG ffmpeg,
      @NotNull ThrowingFunction<FFMPEG, T, Exception> checkHandler,
      @Nullable T defaultValue) {
      if (ffmpeg != null) {
        return checkHandler.apply(ffmpeg);
      }
      return defaultValue;
    }

    /**
     * @return if ffmpeg was started. return true even if thread is dead and getIsAlive() return false
     */
    boolean isRunning();

    @NotNull
    FFMPEGFormat getFormat();

    default void restartIfRequire() {
      if (isRunning() && !getIsAlive()) {
        stopProcessIfNoKeepAlive();
        startConverting();
      }
    }

    void setKeepAlive(int value);

    // just keep key-value metadata for i.e. keep output path
    @NotNull
    JSONObject getMetadata();

    boolean startConverting();

    boolean getIsAlive();

    @NotNull
    Context.FileLogger getFileLogger();

    /**
     * @return true if process was alive and fired stop command, false if process wasn't alive already
     */
    default boolean stopConverting() {
      return stopConverting(null);
    }

    boolean stopConverting(@Nullable Duration waitTimeout);

    /**
     * @return true if process was running, alive and fired stop command, false if process wasn't alive already
     */
    boolean stopProcessIfNoKeepAlive();

    @NotNull
    List<String> getCommandArrayList();

    @NotNull
    Date getCreationDate();

    @NotNull
    String getDescription();

    @NotNull
    String getOutput();

    @NotNull
    Path getOutputFile();

    FFMPEG setWorkingDirectory(@NotNull Path workingDirectory);

    FFMPEG addDestroyListener(@NotNull String key, @NotNull ThrowingRunnable<Exception> destroyListener);
  }

  interface FFMPEGHandler {

    default void ffmpegError(@NotNull String error) {

    }

    default void ffmpegLog(@NotNull Level level, @NotNull String message) {

    }
  }
}
