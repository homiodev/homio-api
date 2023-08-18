package org.homio.api.audio.stream;

import org.homio.api.audio.AudioFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayAudioStream extends FixedLengthAudioStream {

    private final byte[] bytes;
    private final AudioFormat format;
    private final ByteArrayInputStream stream;

    public ByteArrayAudioStream(byte[] bytes, AudioFormat format) {
        this.bytes = bytes;
        this.format = format;
        this.stream = new ByteArrayInputStream(bytes);
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public int read() {
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public long length() {
        return bytes.length;
    }

    @Override
    public InputStream getClonedStream() {
        return new ByteArrayAudioStream(bytes, format);
    }
}
