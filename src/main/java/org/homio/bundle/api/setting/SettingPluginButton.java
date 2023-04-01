package org.homio.bundle.api.setting;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.EntityContext;
import org.homio.bundle.api.ui.field.UIFieldType;
import org.homio.bundle.api.ui.field.action.ActionInputParameter;
import org.homio.bundle.api.util.TouchHomeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public interface SettingPluginButton extends SettingPlugin<JSONObject> {

    default String getConfirmMsg() {
        return "ACTION_CONFIRM_MESSAGE";
    }

    default String getConfirmTitle() {
        return null;
    }

    String getIcon();

    @Override
    default Class<JSONObject> getType() {
        return JSONObject.class;
    }

    @Override
    default UIFieldType getSettingType() {
        return UIFieldType.Button;
    }

    /**
     * In case of action require user input. Dialog popup shows
     * @param entityContext -
     * @param value -
     * @return -
     */
    default List<ActionInputParameter> getInputParameters(EntityContext entityContext, String value) {
        return null;
    }

    default String getInputParametersDialogTitle() {
        return null;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(entityContext, value);
        TouchHomeUtils.putOpt(parameters, "confirm", getConfirmMsg());
        TouchHomeUtils.putOpt(parameters, "title", getConfirmTitle());
        List<ActionInputParameter> actionInputParameters = getInputParameters(entityContext, value);
        if (actionInputParameters != null && !actionInputParameters.isEmpty()) {
            JSONArray inputs = new JSONArray();
            for (ActionInputParameter actionInputParameter : actionInputParameters) {
                inputs.put(actionInputParameter.toJson());
            }
            return parameters.put("inputs", inputs);
        }
        return parameters;
    }

    @Override
    default JSONObject parseValue(EntityContext entityContext, String value) {
        try {
            return new JSONObject(StringUtils.defaultIfEmpty(value, getDefaultValue()));
        } catch (Exception ex) {
            return new JSONObject();
        }
    }
}
