package org.homio.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fazecast.jSerialComm.SerialPort;
import org.homio.api.Context;
import org.homio.api.entity.UserEntity;
import org.homio.api.model.Icon;
import org.homio.api.model.OptionModel.KeyValueEnum;
import org.homio.api.util.HardwareUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.nio.file.Paths;

import static org.homio.api.util.JsonUtils.OBJECT_MAPPER;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface SettingPlugin<T> {

    /**
     * If want to show setting direct on top header panel instead of settings
     */
    default @Nullable String availableForRoute() {
        return null;
    }

    @NotNull
    Class<T> getType();

    default @Nullable Icon getIcon() {
        return null;
    }

    default @NotNull String getDefaultValue() {
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
    default @NotNull JSONObject getParameters(@NotNull Context context, String value) {
        return new JSONObject();
    }

    @NotNull
    SettingType getSettingType();

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
    default boolean isDisabled(Context context) {
        return false;
    }

    // visible on ui or not
    default boolean isVisible(Context context) {
        return true;
    }

    // grouping settings by group name
    default @Nullable String group() {
        return null;
    }

    default @Nullable T deserializeValue(Context context, String value) {
        if (value == null) {
            return null;
        }
        switch (getType().getSimpleName()) {
            case "ObjectNode":
                try {
                    return (T) OBJECT_MAPPER.readValue(value, ObjectNode.class);
                } catch (Exception ignore) {
                    return (T) OBJECT_MAPPER.createObjectNode();
                }
            case "Integer":
                return parseInteger(context, value);
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
                return parseInteger(context, value);
        }
        if (getType().isEnum()) {
            return (T) Enum.valueOf((Class) getType(), value);
        }
        if (SerialPort.class.equals(getType())) {
            return (T) HardwareUtils.getSerialPort(value);
        }
        return (T) value;
    }

    // Is it able to save value to database or local variable
    default boolean isStorable() {
        return getSettingType().isStorable();
    }

    /**
     * Values of settings with transient state doesn't save to db
     *
     * @return is setting is transient
     */
    default boolean transientState() {
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
     * Convert from target type to string
     *
     * @param value -
     * @return -
     */
    default @NotNull String serializeValue(@Nullable T value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    default @NotNull T parseInteger(@NotNull Context context, @NotNull String value) {
        Integer parseValue;
        try {
            parseValue = Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Unable parse setting value <" + value + "> as integer value");
        }
        JSONObject parameters = getParameters(context, value);
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
     * @param context - entity context
     * @param user    - logged in user
     */
    default void assertUserAccess(@NotNull Context context, @Nullable UserEntity user) {
        if (user != null && !user.isAdmin()) {
            user.assertSettingsAccess(this, context);
        }
    }
}
