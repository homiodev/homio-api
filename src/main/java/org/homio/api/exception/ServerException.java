package org.homio.api.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.model.Status;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ServerException extends RuntimeException {

  private @NotNull HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
  private @Nullable Status status;

  private boolean isLog = true;

  public ServerException(@NotNull String message, Status status) {
    super(Lang.getServerMessage(message));
    this.status = status;
  }

  public ServerException(@NotNull String message) {
    this(message, (Status) null);
  }

  public ServerException(@NotNull Exception ex) {
    super(ex);
  }

  public ServerException(@NotNull String message, @NotNull Exception ex) {
    super(Lang.getServerMessage(message), ex);
  }

  public ServerException(@NotNull String message, @NotNull Map<String, Object> messageParam) {
    super(Lang.getServerMessage(message, messageParam));
  }

  public ServerException(@NotNull String message, @NotNull String param0, @NotNull Object value0) {
    this(message, Map.of(param0, value0));
  }

  public ServerException(@NotNull String message, @NotNull Object value0) {
    this(Lang.getServerMessage(message, String.valueOf(value0)));
  }
}
