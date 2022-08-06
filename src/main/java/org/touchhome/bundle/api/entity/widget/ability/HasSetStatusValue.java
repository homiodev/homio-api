package org.touchhome.bundle.api.entity.widget.ability;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.json.JSONObject;
import org.touchhome.bundle.api.EntityContext;
import org.touchhome.bundle.api.model.HasEntityIdentifier;

import java.text.NumberFormat;

/**
 * For widget dataSource to set status value
 */
public interface HasSetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {
    void setStatusValue(SetStatusValueRequest request);

    /**
     * Uses for UI to determine class type description
     */
    @SelectDataSourceDescription
    String getSetStatusDescription();

    @Getter
    @AllArgsConstructor
    class SetStatusValueRequest {
        private EntityContext entityContext;
        private JSONObject dynamicParameters;
        private Object value;

        public static Number rawValueToNumber(Object value, Number defaultValue) {
            if (value == null) return defaultValue;
            if (Number.class.isAssignableFrom(value.getClass())) {
                return ((Number) value);
            }
            try {
                return NumberFormat.getInstance().parse(String.valueOf(value)).floatValue();
            } catch (Exception ignored) {
            }
            return defaultValue;
        }

        public static Boolean rawValueToBoolean(Object value, Boolean defaultValue) {
            if (value == null) return defaultValue;
            if (Boolean.class.isAssignableFrom(value.getClass())) {
                return ((Boolean) value);
            }
            try {
                return Boolean.valueOf(String.valueOf(value));
            } catch (Exception ignored) {
            }
            return defaultValue;
        }

        public float floatValue(float defaultValue) {
            return SetStatusValueRequest.rawValueToNumber(value, defaultValue).floatValue();
        }

        public boolean booleanValue(boolean defaultValue) {
            return SetStatusValueRequest.rawValueToBoolean(value, defaultValue);
        }
    }
}
