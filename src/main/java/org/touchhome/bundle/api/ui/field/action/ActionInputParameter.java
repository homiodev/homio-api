package org.touchhome.bundle.api.ui.field.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.touchhome.bundle.api.util.TouchHomeUtils.putOpt;

@Getter
@Setter
@Accessors(chain = true)
@RequiredArgsConstructor
public class ActionInputParameter {
    public static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z_]+");

    private final String name;
    private final UIActionInput.Type type;
    private final Set<String> validators;
    private final String value;
    private String description;
    private String style;

    public ActionInputParameter(UIActionInput input) {
        this.name = input.name();
        this.type = input.type();
        this.value = input.value();
        this.description = input.description();
        this.validators = new HashSet<>();
        for (UIActionInput.Validator validator : input.validators()) {
            this.validators.add(validator.name());
        }
        if (input.max() < Integer.MAX_VALUE) {
            this.validators.add("max:" + input.max());
        }
        if ((type == UIActionInput.Type.number && input.min() > Integer.MIN_VALUE) || type != UIActionInput.Type.number && input.min() > 0) {
            this.validators.add("min:" + input.min());
        }
        if (!".*".equals(input.pattern().regexp())) {
            this.validators.add("pattern:" + input.pattern().regexp());
        }
    }

    public static ActionInputParameter text(String name, String defaultValue) {
        return new ActionInputParameter(name, UIActionInput.Type.text, null, defaultValue);
    }

    public static ActionInputParameter bool(String name, boolean defaultValue) {
        return new ActionInputParameter(name, UIActionInput.Type.bool, null, String.valueOf(defaultValue));
    }

    public static ActionInputParameter message(String message) {
        return new ActionInputParameter(message, UIActionInput.Type.info, null, null);
    }

    public static ActionInputParameter ip(String name, String defaultIpAddress) {
        return new ActionInputParameter(name, UIActionInput.Type.text,
                Collections.singleton(UIActionInput.Validator.ip.name()), defaultIpAddress);
    }

    public static ActionInputParameter textarea(String name, String value) {
        return new ActionInputParameter(name, UIActionInput.Type.textarea, null, value);
    }

    public JSONObject toJson() {
        if (!NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Wrong name pattern for: " + name);
        }
        JSONObject obj = new JSONObject()
                .put("name", name)
                .put("type", type.name());
        putOpt(obj, "description", StringUtils.trimToNull(description));
        putOpt(obj, "value", value);
        if (validators != null && !validators.isEmpty()) {
            obj.put("validators", validators);
        }
        return obj;
    }
}
