package org.homio.api.audio.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.audio.AudioFormat;
import org.homio.api.exception.NotFoundException;

public class FileAudioStream extends FixedLengthAudioStream {

    public static final String WAV_EXTENSION = "wav";
    public static final String MP3_EXTENSION = "mp3";
    public static final String OGG_EXTENSION = "ogg";
    public static final String AAC_EXTENSION = "aac";

    private final File file;
    private final AudioFormat audioFormat;
    private final long length;
    private InputStream inputStream;

    public FileAudioStream(File file) throws Exception {
        this(file, getAudioFormat(file));
    }

    public FileAudioStream(File file, AudioFormat format) throws Exception {
        this.file = file;
        this.inputStream = getInputStream(file);
        this.audioFormat = format;
        this.length = file.length();
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        super.close();
    }

    @Override
    public long length() {
        return this.length;
    }

    @Override
    public synchronized void reset() throws IOException {
        try {
            inputStream.close();
        } catch (IOException e) {
        }
        try {
            inputStream = getInputStream(file);
        } catch (Exception e) {
            throw new IOException("Cannot reset file input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getClonedStream() throws Exception {
        return getInputStream(file);
    }

    private static AudioFormat getAudioFormat(File file) {
        final String filename = file.getName().toLowerCase();
        final String extension = StringUtils.defaultString(FilenameUtils.getExtension(filename), "");
        switch (extension) {
            case WAV_EXTENSION:
                return new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, 705600,
                    44100L);
            case MP3_EXTENSION:
                return AudioFormat.MP3;
            case OGG_EXTENSION:
                return AudioFormat.OGG;
            case AAC_EXTENSION:
                return AudioFormat.AAC;
            default:
                throw new IllegalArgumentException("Unsupported file extension!");
        }
    }

    private static InputStream getInputStream(File file) throws Exception {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new NotFoundException("File '" + file.getAbsolutePath() + "' not found!");
        }
    }
}
