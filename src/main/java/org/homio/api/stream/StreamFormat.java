package org.homio.api.stream;

import org.apache.commons.io.FilenameUtils;
import org.homio.api.stream.audio.AudioFormat;
import org.homio.api.stream.video.VideoFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MimeType;

import java.util.Objects;

public interface StreamFormat {
    String WAV_EXTENSION = "wav";
    String MP3_EXTENSION = "mp3";
    String OGG_EXTENSION = "ogg";
    String AAC_EXTENSION = "aac";
    String MP4_EXTENSION = "mp4";
    String AVI_EXTENSION = "avi";

    static @NotNull StreamFormat evaluateFormat(@NotNull String filename) {
        final String extension = Objects.toString(FilenameUtils.getExtension(filename), "");
        return switch (extension) {
            case WAV_EXTENSION ->
                    new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, 705600,
                            44100L);
            case MP3_EXTENSION -> AudioFormat.MP3;
            case OGG_EXTENSION -> AudioFormat.OGG;
            case AAC_EXTENSION -> AudioFormat.AAC;
            case MP4_EXTENSION -> VideoFormat.MP4;
            case AVI_EXTENSION -> VideoFormat.AVI;
            default -> throw new IllegalArgumentException("Unsupported file extension!");
        };
    }

    @NotNull MimeType getMimeType();
}
