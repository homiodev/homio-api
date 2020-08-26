package org.touchhome.bundle.api.setting;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.json.NotificationEntityJSON;
import org.touchhome.bundle.api.json.Option;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public interface BundleSettingPlugin<T> {

    default String getIcon() {
        return "";
    }

    default String getToggleIcon() {
        return getIcon();
    }

    default String getIconColor() {
        return "";
    }

    default String getDefaultValue() {
        switch (getSettingType()) {
            case Boolean:
                return Boolean.FALSE.toString();
        }
        return "";
    }

    // min/max/step (Slider)
    default JSONObject getParameters(EntityContext entityContext, String value) {
        return null;
    }

    SettingType getSettingType();

    default T parseValue(EntityContext entityContext, String value) {
        switch (getSettingType()) {
            case Float:
                return (T) Float.valueOf(value);
            case Boolean:
                return (T) Boolean.valueOf(value);
            case Integer:
            case Slider:
                return (T) Integer.valueOf(value);
        }
        return (T) value;
    }

    default List<Option> loadAvailableValues(EntityContext entityContext) {
        throw new IllegalStateException("Must be implemented in sub-classes");
    }

    /**
     * Values of settings with transient state doesn't save to db
     */
    default boolean transientState() {
        return (this.getSettingType() == SettingType.Button && this.getParameters(null, null) == null)
                || this.getSettingType() == SettingType.Info;
    }

    int order();

    /**
     * Advances settings opens in additional panel on ui
     */
    default boolean isAdvanced() {
        return false;
    }

    default NotificationEntityJSON buildToastrNotificationEntity(T value, EntityContext entityContext) {
        return null;
    }

    /**
     * Covnerter from target type to string
     */
    default String writeValue(T value) {
        return value == null ? "" : value.toString();
    }

    @AllArgsConstructor
    enum GroupKey {
        dashboard("fas fa-tachometer-alt"),
        map("fas fa-map"),
        usb("fab fa-usb"),
        system("fas fa-tools");

        @Getter
        private final String icon;
    }

    enum SettingType {
        // Description type uses for showing text inside setting panel on whole width
        Description,
        ColorPicker,
        Float,
        Boolean,
        Integer,
        SelectBox,
        SelectBoxButton,
        // Slider with min/max/step parameters
        Slider,
        // Select box with options fetched from server
        SelectBoxDynamic,
        // Just a text
        Text,
        // Input text with additional button that able to fetch values from server
        TextSelectBoxDynamic,
        // Button that fires server action
        Button,
        Info
    }
}
