package org.touchhome.bundle.api.ui.field.selection.dynamic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.json.JSONObject;
import org.touchhome.bundle.api.entity.HasJsonData;
import org.touchhome.bundle.api.entity.widget.ability.HasGetStatusValue;
import org.touchhome.bundle.api.entity.widget.ability.HasSetStatusValue;
import org.touchhome.bundle.api.model.JSON;

public interface HasDynamicParameterFields extends HasJsonData {

    default JSONObject getDynamicParameterFieldsHolder() {
        return getJsonData().optJSONObject("dsp");
    }

    default void setDynamicParameterFieldsHolder(JSON value) {
        setJsonData("dsp", value);
    }

    default JSONObject getChartDynamicParameterFields() {
        return getDynamicParameterFields("chartDataSource");
    }

    default JSONObject getValueDynamicParameterFields() {
        return getDynamicParameterFields("valueDataSource");
    }

    default JSONObject getSetValueDynamicParameterFields() {
        return getDynamicParameterFields("setValueDataSource");
    }

    default JSONObject getDynamicParameterFields(String key) {
        JSONObject jsonObject = getDynamicParameterFieldsHolder();
        if (jsonObject != null && jsonObject.has(key)) {
            return jsonObject.getJSONObject(key);
        }
        return null;
    }

    /**
     * Specify only in case when need distinguish DynamicParameterClass for different widget type and same entity
     *
     * @param sourceClassType - in case if same entity has few datarsources for same widget. i.e. 1 for action and 2 for fetch
     *                        status
     */
    @JsonIgnore
    default DynamicRequestType getDynamicRequestType(Class<?> sourceClassType) {
        if (sourceClassType.equals(HasGetStatusValue.class)) {
            return DynamicRequestType.GetValue;
        } else if (sourceClassType.equals(HasSetStatusValue.class)) {
            return DynamicRequestType.SetValue;
        }
        return DynamicRequestType.Default;
    }
}
