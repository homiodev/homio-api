package org.homio.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fazecast.jSerialComm.SerialPort;
import java.nio.file.Paths;
import org.homio.api.EntityContext;
import org.homio.api.entity.BaseEntity;
import org.homio.api.entity.UserEntity;
import org.homio.api.model.KeyValueEnum;
import org.homio.api.ui.field.UIFieldType;
import org.homio.api.util.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SettingPlugin<T> {

    /**
     * If want to show setting direct on top header panel instead of settings
     * @return Base entity which setting available to
     */
    default Class<? extends BaseEntity> availableForEntity() {
        return null;
    }

    Class<T> getType();

    // specify max width of rendered ui item. Uses with SelectBox/SelectBoxDynamic
    default Integer getMaxWidth() {
        return null;
    }

    default String getIcon() {
        return "";
    }

    default String getIconColor() {
        return "";
    }

    default String getDefaultValue() {
        switch (getSettingType()) {
            case Integer:
            case Slider:
            case Float:
                return "0";
            case Boolean:
            case Toggle:
                return Boolean.FALSE.toString();
        }
        if (KeyValueEnum.class.isAssignableFrom(getType())) {
            return ((KeyValueEnum) getType().getEnumConstants()[0]).getKey();
        }
        if (getType().isEnum()) {
            return getType().getEnumConstants()[0].toString();
        }
        return "";
    }

    // min/max/step (Slider)
    default JSONObject getParameters(EntityContext entityContext, String value) {
        JSONObject parameters = new JSONObject();
        CommonUtils.putOpt(parameters, "maxWidth", getMaxWidth());
        return parameters;
    }

    UIFieldType getSettingType();

    // if secured - users without admin privileges can't see values
    default boolean isSecuredValue() {
        return false;
    }

    // add revert button to ui
    default boolean isReverted() {
        return false;
    }

    default boolean isRequired() {
        return false;
    }

    // disabled input/button on ui
    default boolean isDisabled(EntityContext entityContext) {
        return false;
    }

    // visible on ui or not
    default boolean isVisible(EntityContext entityContext) {
        return true;
    }

    // grouping settings by group name
    default String group() {
        return null;
    }

    default T parseValue(EntityContext entityContext, String value) {
        if (value == null) {
            return null;
        }
        switch (getType().getSimpleName()) {
            case "Integer":
                return parseInteger(entityContext, value);
            case "Path":
                return (T) Paths.get(value);
        }
        switch (getSettingType()) {
            case Float:
                return (T) Float.valueOf(value);
            case Boolean:
            case Toggle:
                return (T) Boolean.valueOf(value);
            case Integer:
            case Slider:
                return parseInteger(entityContext, value);
        }
        if (getType().isEnum()) {
            return (T) Enum.valueOf((Class) getType(), value);
        }
        if (SerialPort.class.equals(getType())) {
            return (T) CommonUtils.getSerialPort(value);
        }
        return (T) value;
    }

    // Is it able to save value to database or local variable
    default boolean isStorable() {
        return true;
    }

    /**
     * Values of settings with transient state doesn't save to db
     * @return is setting is transient
     */
    default boolean transientState() {
        if (this.getSettingType() == UIFieldType.Button) {
            JSONObject parameters = this.getParameters(null, null);
            if (parameters == null || parameters.length() == 1 && parameters.has("confirm")) {
                return true;
            }
        }
        return !this.isStorable();
    }

    int order();

    /**
     * @return Advances settings opens in additional panel on ui
     */
    default boolean isAdvanced() {
        return false;
    }

    /**
     * Covnerter from target type to string
     * @param value -
     * @return -
     */
    default String writeValue(T value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    default T parseInteger(EntityContext entityContext, String value) {
        Integer parseValue;
        try {
            parseValue = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable parse setting value <" + value + "> as integer value");
        }
        JSONObject parameters = getParameters(entityContext, value);
        if (parameters != null) {
            if (parameters.has("min") && parseValue < parameters.getInt("min")) {
                throw new IllegalArgumentException(
                    "Setting value <" + value + "> less than minimum value: " + parameters.getInt("min"));
            }
            if (parameters.has("max") && parseValue > parameters.getInt("max")) {
                throw new IllegalArgumentException(
                    "Setting value <" + value + "> more than maximum value: " + parameters.getInt("max"));
            }
        }
        return (T) parseValue;
    }

    /**
     * Assert that user has access to change setting
     *
     * @param entityContext - entity context
     * @param user          - logged in user
     * @throws IllegalAccessException - access denied
     */
    default void assertUserAccess(@NotNull EntityContext entityContext, @Nullable UserEntity user) throws IllegalAccessException {
        if (user == null || !user.isAdmin()) {
            throw new IllegalAccessException();
        }
    }
}