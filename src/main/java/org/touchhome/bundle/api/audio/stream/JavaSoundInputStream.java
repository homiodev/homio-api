package org.touchhome.bundle.api.audio.stream;

import javax.sound.sampled.TargetDataLine;
import org.jetbrains.annotations.Nullable;
import org.touchhome.bundle.api.audio.AudioFormat;
import org.touchhome.bundle.api.audio.AudioStream;

public class JavaSoundInputStream extends AudioStream {

    /** TargetDataLine for the input */
    private final TargetDataLine input;

    private final AudioFormat format;

    /**
     * Constructs a JavaSoundInputStream with the passed input
     *
     * @param input The mic which data is pulled from
     */
    public JavaSoundInputStream(TargetDataLine input, AudioFormat format) {
        this.format = format;
        this.input = input;
        this.input.start();
    }

    @Override
    public int read() {
        byte[] b = new byte[1];

        int bytesRead = read(b);

        if (-1 == bytesRead) {
            return bytesRead;
        }

        Byte bb = b[0];
        return bb.intValue();
    }

    @Override
    public int read(byte[] b) {
        return input.read(b, 0, b.length);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) {
        return input.read(b, off, len);
    }

    @Override
    public void close() {
        input.close();
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }
}
