package org.homio.api.stream.audio;

import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.TargetDataLine;
import lombok.Getter;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;

public class JavaSoundInputStream extends AbstractResource implements ContentStream {

  /** TargetDataLine for the input */
  private final @NotNull TargetDataLine input;

  @Getter private final @NotNull StreamFormat streamFormat;
  private final InputStream inputStream;

  public JavaSoundInputStream(@NotNull TargetDataLine input, @NotNull AudioFormat streamFormat) {
    this.input = input;
    this.streamFormat = streamFormat;
    this.inputStream =
        new InputStream() {
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
          public int read(byte @NotNull [] b) {
            return input.read(b, 0, b.length);
          }

          @Override
          public int read(byte @Nullable [] b, int off, int len) {
            return input.read(b, off, len);
          }

          @Override
          public void close() throws IOException {
            super.close();
            input.close();
          }
        };
  }

  @Override
  public long contentLength() {
    return -1;
  }

  @Override
  public @NotNull InputStream getInputStream() throws IllegalStateException {
    input.start();
    return inputStream;
  }

  @Override
  public @NotNull String getDescription() {
    return getClass().getSimpleName() + ": " + input.getLineInfo().toString();
  }

  @Override
  public @NotNull Resource getResource() {
    return this;
  }
}
