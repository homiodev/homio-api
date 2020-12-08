package org.touchhome.bundle.api.exception;

import lombok.Getter;
import org.touchhome.bundle.api.util.FlowMap;

public class UserException extends RuntimeException {
    @Getter
    private FlowMap messageParam;

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, FlowMap messageParam) {
        super(message);
        this.messageParam = messageParam;
    }
}
