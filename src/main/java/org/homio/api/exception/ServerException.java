package org.homio.api.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.homio.api.util.FlowMap;
import org.homio.api.util.Lang;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;

public class ServerException extends RuntimeException {

    @Getter
    @Setter
    @Accessors(chain = true)
    private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public ServerException(@NotNull String message) {
        super(Lang.getServerMessage(message));
    }

    public ServerException(@NotNull Exception ex) {
        super(ex);
    }

    public ServerException(@NotNull String message, @NotNull Exception ex) {
        super(Lang.getServerMessage(message), ex);
    }

    public ServerException(@NotNull String message, @NotNull FlowMap messageParam) {
        super(Lang.getServerMessage(message, messageParam));
    }

    public ServerException(@NotNull String message, @NotNull String param0, @NotNull Object value0) {
        this(message, FlowMap.of(param0, value0));
    }

    public ServerException(@NotNull String message, @NotNull Object value0) {
        this(Lang.getServerMessage(message, String.valueOf(value0)));
    }
}
