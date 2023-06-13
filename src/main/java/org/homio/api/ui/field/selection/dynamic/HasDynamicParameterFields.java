package org.homio.api.ui.field.selection.dynamic;

import org.homio.api.entity.HasJsonData;
import org.homio.api.model.JSON;
import org.json.JSONObject;

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
}
