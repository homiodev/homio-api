package org.touchhome.bundle.api.model;

import lombok.Getter;

import static org.touchhome.bundle.api.util.TouchHomeUtils.getErrorMessage;


@Getter
public class ErrorHolderModel {
    private final String title;
    private final String message;
    private final String cause;
    private final String errorType;

    public ErrorHolderModel(String title, String message, Exception ex) {
        this.title = title;
        this.message = message;
        this.cause = getErrorMessage(ex);
        this.errorType = ex.getClass().getSimpleName();
    }
}
