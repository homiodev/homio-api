package org.homio.bundle.api.ui.field.action;

import static org.homio.bundle.api.util.CommonUtils.putOpt;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.homio.bundle.api.model.OptionModel;
import org.json.JSONObject;

// TODO:  ???????????????
@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class ActionInputParameter {
    public static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_.]+");

    private final String name;
    private final UIActionInput.Type type;
    private final Set<String> validators;
    private final String value;
    private String description;
    private String style;
    private List<OptionModel> options;

    public ActionInputParameter(UIActionInput input) {
        this.name = input.name();
        this.type = input.type();
        this.value = input.value();
        this.description = input.description();
        this.validators = new HashSet<>();
        /*for (UIActionInput.Validator validator : input.validators()) {
            this.validators.add(validator.name());
        }*/
        if (input.max() < Integer.MAX_VALUE) {
            this.validators.add("max:" + input.max());
        }
        if ((type == UIActionInput.Type.number && input.min() > Integer.MIN_VALUE) ||
                type != UIActionInput.Type.number && input.min() > 0) {
            this.validators.add("min:" + input.min());
        }
        if (!".*".equals(input.pattern().regexp())) {
            this.validators.add("pattern:" + input.pattern().regexp());
        }
    }

    public static ActionInputParameter text(String name, String defaultValue, String... validators) {
        return new ActionInputParameter(name, UIActionInput.Type.text, Set.of(validators), defaultValue);
    }

    public static ActionInputParameter email(String name, String defaultValue) {
        return new ActionInputParameter(name, UIActionInput.Type.text, Collections.singleton("email"), defaultValue);
    }

    public static ActionInputParameter password(String name, String defaultValue) {
        return new ActionInputParameter(name, UIActionInput.Type.password, Collections.singleton("password"), defaultValue);
    }

    public static ActionInputParameter bool(String name, boolean defaultValue) {
        return new ActionInputParameter(name, UIActionInput.Type.bool, null, String.valueOf(defaultValue));
    }

    public static ActionInputParameter message(String message) {
        return new ActionInputParameter(message, UIActionInput.Type.info, null, null);
    }

    public static ActionInputParameter ip(String name, String defaultIpAddress) {
        return new ActionInputParameter(name, UIActionInput.Type.ip,
                Collections.singleton("ip"), defaultIpAddress);
    }

    public static ActionInputParameter textarea(String name, String value) {
        return new ActionInputParameter(name, UIActionInput.Type.textarea, null, value);
    }

    // Options example: 1:true;0:false or 1;2;3
    public static ActionInputParameter select(String name, String value, List<OptionModel> options) {
        return new ActionInputParameter(name, UIActionInput.Type.select, null, value).setOptions(options);
    }

    public JSONObject toJson() {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Wrong name pattern for: " + name);
        }
        JSONObject obj = new JSONObject().put("name", name).put("type", type.name());
        putOpt(obj, "description", StringUtils.trimToNull(description));
        putOpt(obj, "value", value);
        putOpt(obj, "options", options);
        if (validators != null && !validators.isEmpty()) {
            obj.put("validators", validators);
        }
        return obj;
    }
}
