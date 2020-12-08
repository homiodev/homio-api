package org.touchhome.bundle.api.json;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.touchhome.bundle.api.manager.En;

@Getter
@Setter
@Accessors(chain = true)
public class ActionResponse {
    private final Object value;
    private ResponseAction responseAction = ResponseAction.ShowInfoMsg;

    public ActionResponse(Object value) {
        this.value = value instanceof String ? En.getServerMessage((String) value) : value;
    }

    public ActionResponse(Object value, String param0, String value0) {
        this.value = value instanceof String ? En.getServerMessage((String) value, param0, value0) : value;
    }

    public ActionResponse(Object value, String param0, String value0, ResponseAction responseAction) {
        this(value, param0, value0);
        this.responseAction = responseAction;
    }

    public ActionResponse(Object value, ResponseAction responseAction) {
        this(value);
        this.responseAction = responseAction;
    }

    public enum ResponseAction {
        ShowInfoMsg, ShowErrorMsg, ShowWarnMsg, ShowSuccessMsg, ShowJson
    }
}
