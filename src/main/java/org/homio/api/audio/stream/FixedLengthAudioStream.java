package org.homio.api.audio.stream;

import java.io.InputStream;
import org.homio.api.audio.AudioStream;

public abstract class FixedLengthAudioStream extends AudioStream {

    /**
     * Provides the length of the stream in bytes.
     *
     * @return absolute length in bytes
     */
    public abstract long length();

    /**
     * Returns a new, fully independent stream instance, which can be read and closed without impacting the original instance.
     *
     * @return a new input stream that can be consumed by the caller
     * @throws Exception -
     */
    public abstract InputStream getClonedStream() throws Exception;
}
