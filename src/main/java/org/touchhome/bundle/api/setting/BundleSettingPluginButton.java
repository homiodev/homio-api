package org.touchhome.bundle.api.setting;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;

import java.util.List;
import java.util.regex.Pattern;

public interface BundleSettingPluginButton extends BundleSettingPlugin<JSONObject> {

    Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_]+");

    @Override
    default SettingType getSettingType() {
        return SettingType.Button;
    }

    default List<InputParameter> getInputParameters(EntityContext entityContext, String value) {
        return null;
    }

    @Override
    default JSONObject getParameters(EntityContext entityContext, String value) {
        List<InputParameter> inputParameters = getInputParameters(entityContext, value);
        if (inputParameters != null && !inputParameters.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (InputParameter inputParameter : inputParameters) {
                if (!NAME_PATTERN.matcher(inputParameter.name).matches()) {
                    throw new IllegalArgumentException("Wrong name pattern for: " + inputParameter.name);
                }
                JSONObject obj = new JSONObject()
                        .put("name", inputParameter.name)
                        .put("type", inputParameter.type.name())
                        .put("value", inputParameter.value);
                if (StringUtils.isNotEmpty(inputParameter.description)) {
                    obj.put("description", inputParameter.description);
                }
                if (inputParameter.validator != null) {
                    obj.put("validator", inputParameter.validator.name());
                }
                jsonArray.put(obj);
            }
            return new JSONObject().put("parameters", jsonArray);
        }
        return null;
    }

    @Override
    default JSONObject parseValue(EntityContext entityContext, String value) {
        try {
            return new JSONObject(StringUtils.defaultIfEmpty(value, getDefaultValue()));
        } catch (Exception ex) {
            return new JSONObject();
        }
    }

    @Setter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    class InputParameter {
        private final String name;
        private final InputParameterType type;
        private final InputParameterValidator validator;
        private final String value;
        private String description;
    }

    enum InputParameterType {
        text, json, textarea, password
    }

    enum InputParameterValidator {
        ip, password, email
    }
}
