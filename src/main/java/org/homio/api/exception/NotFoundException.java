package org.homio.api.exception;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

import java.nio.file.Path;
import java.util.Map;

public class NotFoundException extends ServerException {

  public NotFoundException(String message) {
    super(message);
    setHttpStatus(HttpStatus.NOT_FOUND);
  }

  public NotFoundException(@NotNull String message, @NotNull Map<String, Object> messageParam) {
    super(message, messageParam);
    setHttpStatus(HttpStatus.NOT_FOUND);
  }

  public NotFoundException(@NotNull String message, @NotNull String param0,
                           @NotNull Object value0) {
    super(message, param0, value0);
    setHttpStatus(HttpStatus.NOT_FOUND);
  }

  public NotFoundException(@NotNull String message, @NotNull Object value0) {
    super(message, value0);
    setHttpStatus(HttpStatus.NOT_FOUND);
  }

  public static NotFoundException entityNotFound(@NotNull String title) {
    return new NotFoundException("ERROR.ENTITY_NOT_FOUND", title);
  }

  public static NotFoundException fileNotFound(Path path) {
    return fileNotFound(path.toAbsolutePath().toString());
  }

  public static NotFoundException fileNotFound(String path) {
    return new NotFoundException("ERROR.FILE_NOT_FOUND", path);
  }
}
