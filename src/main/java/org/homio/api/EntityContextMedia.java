package org.homio.api;

import com.pivovarit.function.ThrowingConsumer;
import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingRunnable;
import java.awt.Dimension;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.logging.log4j.Level;
import org.homio.api.state.DecimalType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public interface EntityContextMedia {

    @NotNull EntityContext getEntityContext();

    void fireFfmpeg(@NotNull String inputOptions, @NotNull String source, @NotNull String output, int maxWaitTimeout);

    void registerMediaMTXSource(@NotNull String path, @NotNull MediaMTXSource source);

    void unRegisterMediaMTXSource(@NotNull String path);

    @NotNull VideoInputDevice createVideoInputDevice(@NotNull String vfile);

    /**
     * @return - Get usb camera
     */
    @NotNull Set<String> getVideoDevices();

    /**
     * @return - Get audio devices
     */
    @NotNull Set<String> getAudioDevices();

    @NotNull FFMPEG buildFFMPEG(@NotNull String entityID,
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
    }

    interface VideoInputDevice {

        @NotNull String getName();

        @NotNull VideoInputDevice setName(@NotNull String value);

        @NotNull Dimension[] getResolutions();

        default @NotNull Set<String> getResolutionSet() {
            Dimension[] resolutions = getResolutions();
            return Arrays.stream(resolutions).sorted(Comparator.comparingInt(o -> o.width + o.height))
                         .map(r -> String.format("%dx%d", r.width, r.height)).collect(Collectors.toCollection(LinkedHashSet::new));
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
        static <T> T check(@Nullable FFMPEG ffmpeg, @NotNull ThrowingFunction<FFMPEG, T, Exception> checkHandler, @Nullable T defaultValue) {
            if (ffmpeg != null) {
                return checkHandler.apply(ffmpeg);
            }
            return defaultValue;
        }

        /**
         * @return if ffmpeg was started. return true even if thread is dead and getIsAlive() return false
         */
        boolean isRunning();

        @NotNull FFMPEGFormat getFormat();

        default void restartIfRequire() {
            if (isRunning() && !getIsAlive()) {
                stopProcessIfNoKeepAlive();
                startConverting();
            }
        }

        void setKeepAlive(int value);

        // just keep key-value metadata for i.e. keep output path
        @NotNull JSONObject getMetadata();

        boolean startConverting();

        boolean getIsAlive();

        Path getLogPath();

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

        @NotNull List<String> getCommandArrayList();

        @NotNull Date getCreationDate();

        @NotNull String getDescription();

        @NotNull String getOutput();

        @NotNull Path getOutputFile();

        FFMPEG setWorkingDirectory(@NotNull Path workingDirectory);

        FFMPEG addDestroyListener(@NotNull String key, @NotNull ThrowingRunnable<Exception> destroyListener);
    }

    interface FFMPEGHandler {

        default void motionDetected(boolean on) {

        }

        default void audioDetected(boolean on) {

        }

        default void ffmpegError(@NotNull String error) {

        }

        default @NotNull DecimalType getMotionThreshold() {
            return new DecimalType(40);
        }

        default void ffmpegLog(@NotNull Level level, @NotNull String message) {

        }
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    class MediaMTXSource {

        private final String source;
        private boolean sourceOnDemand = true;
        private boolean sourceAnyPortEnable = false;
        private SourceProtocol sourceProtocol = SourceProtocol.automatic;

        public enum SourceProtocol {
            automatic, udp, multicast, tcp
        }
    }
}
