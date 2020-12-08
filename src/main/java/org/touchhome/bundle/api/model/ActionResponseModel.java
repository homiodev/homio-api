package org.touchhome.bundle.api.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.manager.En;

import java.util.Collections;
import java.util.Set;

@Getter
@Setter
@Accessors(chain = true)
public class ActionResponseModel {
    private final Object value;
    private ResponseAction responseAction = ResponseAction.info;

    public static ActionResponseModel showJson(String content) {
        return showFiles(Collections.singleton(new FileModel(null, content, FileContentType.json, true)));
    }

    public static ActionResponseModel showFiles(Set<FileModel> fileModels) {
        return new ActionResponseModel(fileModels, ResponseAction.files);
    }

    public static ActionResponseModel showInfo(Object value) {
        return new ActionResponseModel(value, ResponseAction.info);
    }

    public static ActionResponseModel showWarn(Object value) {
        return new ActionResponseModel(value, ResponseAction.warning);
    }

    public static ActionResponseModel showError(Object value) {
        return new ActionResponseModel(value, ResponseAction.error);
    }

    public static ActionResponseModel showSuccess(Object value) {
        return new ActionResponseModel(value, ResponseAction.success);
    }

    public static ActionResponseModel showInfo(Object value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.info);
    }

    public static ActionResponseModel showWarn(Object value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.warning);
    }

    public static ActionResponseModel showError(Object value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.error);
    }

    public static ActionResponseModel showSuccess(Object value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.success);
    }

    private ActionResponseModel(Object value, String param0, String value0) {
        this.value = value instanceof String ? En.getServerMessage((String) value, param0, value0) : value;
    }

    private ActionResponseModel(Object value, String param0, String value0, ResponseAction responseAction) {
        this(value, param0, value0);
        this.responseAction = responseAction;
    }

    private ActionResponseModel(Object value, ResponseAction responseAction) {
        this(value);
        this.responseAction = responseAction;
    }

    private ActionResponseModel(Object value) {
        this.value = value instanceof String ? En.getServerMessage((String) value) : value;
    }

    public enum ResponseAction {
        info, error, warning, success, files
    }
}
