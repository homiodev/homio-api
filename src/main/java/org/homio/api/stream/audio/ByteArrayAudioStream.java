package org.homio.api.stream.audio;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;

@Getter
public class ByteArrayAudioStream extends ByteArrayResource implements AudioStream {

    private final @NotNull AudioFormat format;

    public ByteArrayAudioStream(byte[] byteArray, @NotNull AudioFormat format) {
        super(byteArray);
        this.format = format;
    }
}
