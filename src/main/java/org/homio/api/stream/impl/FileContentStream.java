package org.homio.api.stream.impl;

import lombok.Getter;
import org.homio.api.stream.ContentStream;
import org.homio.api.stream.StreamFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Objects;

@Getter
public class FileContentStream extends FileSystemResource implements ContentStream {

  private final @NotNull StreamFormat streamFormat;

  public FileContentStream(@NotNull File file) {
    this(file, null);
  }

  public FileContentStream(@NotNull File file, @Nullable StreamFormat streamFormat) {
    super(file);
    this.streamFormat =
        Objects.requireNonNullElseGet(
            streamFormat, () -> StreamFormat.evaluateFormat(file.getName()));
  }

  @Override
  public @NotNull Resource getResource() {
    return this;
  }
}
