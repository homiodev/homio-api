package org.homio.api.stream.impl;

import lombok.Getter;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

@Getter
public class ByteArrayContentStream extends ByteArrayResource implements ContentStream {

  private final @NotNull StreamFormat streamFormat;

  public ByteArrayContentStream(byte[] byteArray, @NotNull StreamFormat streamFormat) {
    super(byteArray);
    this.streamFormat = streamFormat;
  }

  @Override
  public @NotNull Resource getResource() {
    return this;
  }
}
