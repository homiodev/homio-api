package org.homio.api.model;

import lombok.Getter;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.Lang;


@Getter
public class ErrorHolderModel {
    private final String title;
    private final String message;
    private final String cause;
    private final String errorType;

    public ErrorHolderModel(String title, String message, Throwable ex) {
        this.title = title;
        this.message = message == null ? null : Lang.getServerMessage(message);
        this.cause = CommonUtils.getErrorMessage(ex);
        this.errorType = ex.getClass().getSimpleName();
    }
}
