package org.homio.api.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException entityNotFound(String entityID) {
        return new NotFoundException("Unable to find entity: " + entityID);
    }
}
