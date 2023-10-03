package org.homio.api.ui.field.selection.dynamic;

import java.util.Map;
import java.util.Map.Entry;
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
            Object obj = jsonObject.get(key);
            if (obj instanceof Map<?, ?> map) {
                JSONObject json = new JSONObject();
                for (Entry<?, ?> entry : map.entrySet()) {
                    json.put(entry.getKey().toString(), entry.getValue());
                }
                return json;
            }
            return jsonObject.getJSONObject(key);
        }
        return null;
    }
}
