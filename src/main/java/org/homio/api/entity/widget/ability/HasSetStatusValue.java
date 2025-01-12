package org.homio.api.entity.widget.ability;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.homio.api.Context;
import org.homio.api.model.HasEntityIdentifier;
import org.json.JSONObject;

import java.text.NumberFormat;

/**
 * For widget dataSource to set status value
 */
public interface HasSetStatusValue extends HasEntityIdentifier, HasUpdateValueListener {

  void setStatusValue(SetStatusValueRequest request);

  HasGetStatusValue.ValueType getValueType();

  /**
   * @return Some entites that implement HasSetStatusValue may be 'readOnly', thus this entities must be skipped
   */
  @JsonIgnore
  default boolean isAbleToSetValue() {
    return true;
  }

  @Getter
  @AllArgsConstructor
  class SetStatusValueRequest {

    private @Accessors(fluent = true) Context context;
    private JSONObject dynamicParameters;
    private Object value;

    public static Number rawValueToNumber(Object value, Number defaultValue) {
      if (value == null) {
        return defaultValue;
      }
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
      if (value == null) {
        return defaultValue;
      }
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
