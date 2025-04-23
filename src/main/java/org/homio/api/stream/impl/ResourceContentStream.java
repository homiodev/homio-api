package org.homio.api.stream.impl;

import lombok.Getter;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.Resource;

@Getter
public class ResourceContentStream implements ContentStream {

  private final @NotNull StreamFormat streamFormat;
  @Getter private final Resource resource;

  public ResourceContentStream(@NotNull Resource resource, @NotNull StreamFormat streamFormat) {
    this.streamFormat = streamFormat;
    this.resource = resource;
  }
}
