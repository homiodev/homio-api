package org.homio.bundle.api.audio;

import java.util.Set;

public interface AudioSource {
    /**
     * Returns a simple string that uniquely identifies this service
     *
     * @return an id that identifies this service
     */
    String getEntityID();

    /**
     * Obtain the audio formats supported by this AudioSource
     *
     * @return The audio formats supported by this service
     */
    Set<AudioFormat> getSupportedFormats();

    /**
     * Gets an AudioStream for reading audio data in supported audio format
     *
     * @param format the expected audio format of the stream
     * @return AudioStream for reading audio data
     * @throws Exception If problem occurs obtaining the stream
     */
    AudioStream getInputStream(AudioFormat format) throws Exception;
}
