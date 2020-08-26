package org.touchhome.bundle.api.json;

import lombok.Getter;
import org.touchhome.bundle.api.util.TouchHomeUtils;

@Getter
public class ErrorHolder {
    private String title;
    private String message;
    private String cause;
    private String errorType;

    public ErrorHolder(String title, String message, Exception ex) {
        this.title = title;
        this.message = message;
        this.cause = TouchHomeUtils.getErrorMessage(ex);
        this.errorType = ex.getClass().getSimpleName();
    }
}
