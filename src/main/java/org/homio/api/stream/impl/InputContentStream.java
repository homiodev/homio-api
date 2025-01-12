package org.homio.api.stream.impl;

import lombok.Getter;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Getter
public class InputContentStream extends InputStreamResource implements ContentStream {

  private final @NotNull StreamFormat streamFormat;

  public InputContentStream(InputStream inputStream, @NotNull StreamFormat streamFormat) {
    super(inputStream);
    this.streamFormat = streamFormat;
  }

  @Override
  public @NotNull Resource getResource() {
    return this;
  }
}
