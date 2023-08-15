package org.homio.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.homio.api.util.CommonUtils;
import org.homio.api.util.Lang;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Setter
@Accessors(chain = true)
public class ActionResponseModel {
    private final Object value;
    private ResponseAction responseAction = ResponseAction.info;

    private ActionResponseModel(Object value, String param0, String value0) {
        this.value = value instanceof String ? Lang.getServerMessage((String) value, param0, value0) : value;
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
        this.value = value instanceof String ? Lang.getServerMessage((String) value) : value;
    }

    @SneakyThrows
    public static ActionResponseModel showJson(String title, Object value) {
        String content;
        if (value instanceof JSONObject || value instanceof JSONArray) {
            content = value.toString();
        } else {
            content = new ObjectMapper().writeValueAsString(value);
        }
        return showFiles(Collections.singleton(new FileModel(title, content, FileContentType.json, true)));
    }

    public static ActionResponseModel showFiles(Set<FileModel> fileModels) {
        return new ActionResponseModel(fileModels, ResponseAction.files);
    }

    public static ActionResponseModel showFile(FileModel fileModel) {
        return ActionResponseModel.showFiles(Set.of(fileModel));
    }

    public static ActionResponseModel showInfoAlreadyDone() {
        return new ActionResponseModel("W.INFO.ALREADY_DONE", ResponseAction.info);
    }

    public static ActionResponseModel showInfo(String value) {
        return new ActionResponseModel(value, ResponseAction.info);
    }

    public static ActionResponseModel showWarn(String value) {
        return new ActionResponseModel(value, ResponseAction.warning);
    }

    public static ActionResponseModel showError(String value) {
        return new ActionResponseModel(value, ResponseAction.error);
    }

    public static ActionResponseModel showError(Exception ex) {
        return new ActionResponseModel(CommonUtils.getErrorMessage(ex), ResponseAction.error);
    }

    public static ActionResponseModel success() {
        return new ActionResponseModel("ACTION.RESPONSE.SUCCESS", ResponseAction.success);
    }

    public static ActionResponseModel fired() {
        return new ActionResponseModel("ACTION.RESPONSE.FIRED", ResponseAction.info);
    }

    public static ActionResponseModel showSuccess(String value) {
        return new ActionResponseModel(value, ResponseAction.success);
    }

    public static ActionResponseModel showInfo(String value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.info);
    }

    public static ActionResponseModel showInfo(String value, String value0) {
        return new ActionResponseModel(value, "VALUE", value0, ResponseAction.info);
    }

    public static ActionResponseModel showWarn(String value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.warning);
    }

    public static ActionResponseModel showWarn(String value, String value0) {
        return new ActionResponseModel(value, "VALUE", value0, ResponseAction.warning);
    }

    public static ActionResponseModel showError(String value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.error);
    }

    public static ActionResponseModel showError(String value, String value0) {
        return new ActionResponseModel(value, "VALUE", value0, ResponseAction.error);
    }

    public static ActionResponseModel showSuccess(String value, String param0, String value0) {
        return new ActionResponseModel(value, param0, value0, ResponseAction.success);
    }

    public enum ResponseAction {
        info, error, warning, success, files
    }
}
