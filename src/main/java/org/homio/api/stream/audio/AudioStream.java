package org.homio.api.stream.audio;

import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.homio.api.stream.ContentStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * File/Url/... audio stream
 */
public interface AudioStream extends ContentStream, Resource {

    String WAV_EXTENSION = "wav";
    String MP3_EXTENSION = "mp3";
    String OGG_EXTENSION = "ogg";
    String AAC_EXTENSION = "aac";

    @SneakyThrows
    static @NotNull AudioStream fromUnknownStream(@NotNull InputStream stream, @Nullable AudioFormat audioFormat) {
        if (audioFormat == null) {
            audioFormat = AudioFormat.MP3;
        }
        return new ByteArrayAudioStream(IOUtils.toByteArray(stream), audioFormat);
    }

    static AudioFormat evaluateFormat(String filename) {
        final String extension = Objects.toString(FilenameUtils.getExtension(filename), "");
        return switch (extension) {
            case WAV_EXTENSION ->
                    new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, 705600,
                            44100L);
            case MP3_EXTENSION -> AudioFormat.MP3;
            case OGG_EXTENSION -> AudioFormat.OGG;
            case AAC_EXTENSION -> AudioFormat.AAC;
            default -> throw new IllegalArgumentException("Unsupported file extension!");
        };
    }

    @NotNull
    AudioFormat getFormat();

    default @Nullable String getMimeType() {
        if (AudioFormat.CODEC_MP3.equals(getFormat().getCodec())) {
            return "audio/mpeg";
        } else if (AudioFormat.CONTAINER_WAVE.equals(getFormat().getContainer())) {
            return "audio/wav";
        } else if (AudioFormat.CONTAINER_OGG.equals(getFormat().getContainer())) {
            return "audio/ogg";
        }
        return null;
    }

    @Override
    default @NotNull Resource getResource() {
        return this;
    }

    @Override
    default void close() throws IOException {
    }
}
