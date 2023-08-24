package org.homio.api;

import java.awt.Dimension;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.logging.log4j.Logger;
import org.homio.api.state.DecimalType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EntityContextMedia {

    String CHANNEL_FFMPEG_MOTION_ALARM = "ffmpegMotionAlarm";

    @NotNull EntityContext getEntityContext();

    void fireFfmpeg(@NotNull String inputOptions, @NotNull String source, @NotNull String output, int maxWaitTimeout);

    VideoInputDevice createVideoInputDevice(@NotNull String vfile);

    Set<String> getVideoDevices();

    Set<String> getAudioDevices();

    @NotNull FFMPEG buildFFMPEG(@NotNull String entityID,
                                @NotNull String description,
                                @NotNull FFMPEGHandler handler,
                                @NotNull Logger log,
                                @NotNull FFMPEGFormat format,
                                @NotNull String inputArguments,
                                @NotNull String input,
                                @NotNull String outArguments,
                                @NotNull String output,
                                @NotNull String username,
                                @NotNull String password,
                                @Nullable Runnable destroyListener);

    enum FFMPEGFormat {
        HLS,
        GIF,
        RECORD,
        RTSP_ALARMS,
        MJPEG,
        SNAPSHOT,
        MUXER
    }

    interface VideoInputDevice {

        String getName();

        VideoInputDevice setName(String value);

        Dimension getResolution();

        Dimension[] getResolutions();

        default String getResolutionString() {
            Dimension d = getResolution();
            return String.format("%dx%d", d.width, d.height);
        }
    }

    interface FFMPEG {

        static void run(@Nullable FFMPEG ffmpeg, @NotNull Consumer<FFMPEG> handler) {
            if (ffmpeg != null) {
                handler.accept(ffmpeg);
            }
        }

        static <T> T execute(@Nullable FFMPEG ffmpeg, @NotNull Function<FFMPEG, T> handler) {
            if (ffmpeg != null) {
                return handler.apply(ffmpeg);
            }
            return null;
        }

        /**
         * @return if ffmpeg was started. return true even if thread is dead and getIsAlive() return false
         */
        boolean isRunning();

        default void restartIfRequire() {
            if (isRunning() && !getIsAlive()) {
                stopProcessIfNoKeepAlive();
                startConverting();
            }
        }

        void setKeepAlive(int value);

        boolean startConverting();

        boolean getIsAlive();

        void stopConverting();

        boolean stopProcessIfNoKeepAlive();

        List<String> getCommandArrayList();

        @NotNull Date getCreationDate();

        String getDescription();
    }

    interface FFMPEGHandler {

        String getEntityID();

        void motionDetected(boolean on, String key);

        void audioDetected(boolean on);

        void ffmpegError(String error);

        DecimalType getMotionThreshold();
    }
}
