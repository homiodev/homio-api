package org.touchhome.bundle.api.exception;

import lombok.Getter;
import org.touchhome.bundle.api.util.FlowMap;

public class ServerException extends RuntimeException {
    @Getter
    private FlowMap messageParam;

    public ServerException(String message) {
        super(message);
    }

    public ServerException(Exception ex) {
        super(ex);
    }

    public ServerException(String message, Exception ex) {
        super(message, ex);
    }

    public ServerException(String message, FlowMap messageParam) {
        super(message);
        this.messageParam = messageParam;
    }
}
