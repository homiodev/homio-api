package org.homio.api.setting;

import static org.homio.api.util.JsonUtils.putOpt;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.homio.api.Context;
import org.homio.api.model.Icon;
import org.homio.api.ui.field.action.ActionInputParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public interface SettingPluginButton extends SettingPlugin<JSONObject> {

    /**
     * Confirm message when click on button. null if not requires
     *
     * @return message
     */
    @Nullable
    String getConfirmMsg();

    default @Nullable String getDialogColor() {
        return null;
    }

    default @Nullable String getConfirmTitle() {
        return null;
    }

    @Nullable
    Icon getIcon();

    default @Nullable String getText() {
        return null;
    }

    default @Nullable String getText(Context context) {
        return getText();
    }

    /**
     * Does show button as 'primary'
     *
     * @return primary or not
     */
    default boolean isPrimary() {
        return false;
    }

    @Override
    default @NotNull Class<JSONObject> getType() {
        return JSONObject.class;
    }

    @Override
    default @NotNull SettingType getSettingType() {
        return SettingType.Button;
    }

    /**
     * In case of action require user input. Dialog popup shows
     *
     * @param context -
     * @param value   -
     * @return -
     */
    default List<ActionInputParameter> getInputParameters(Context context, String value) {
        return null;
    }

    default String getInputParametersDialogTitle() {
        return null;
    }

    @Override
    default @NotNull JSONObject getParameters(Context context, String value) {
        JSONObject parameters = SettingPlugin.super.getParameters(context, value);
        putOpt(parameters, "confirm", getConfirmMsg());
        putOpt(parameters, "dialogColor", getDialogColor());
        putOpt(parameters, "title", getConfirmTitle());
        List<ActionInputParameter> actionInputParameters = getInputParameters(context, value);
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
    default JSONObject deserializeValue(Context context, String value) {
        try {
            return new JSONObject(StringUtils.defaultIfEmpty(value, getDefaultValue()));
        } catch (Exception ex) {
            return new JSONObject();
        }
    }
}
