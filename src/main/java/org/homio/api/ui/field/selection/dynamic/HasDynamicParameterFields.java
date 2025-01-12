package org.homio.api.ui.field.selection.dynamic;

import org.homio.api.entity.HasJsonData;
import org.homio.api.model.JSON;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;
import java.util.Map.Entry;

public interface HasDynamicParameterFields extends HasJsonData {

  default @Nullable JSONObject getDynamicParameterFieldsHolder() {
    return getJsonData().optJSONObject("dsp");
  }

  default void setDynamicParameterFieldsHolder(JSON value) {
    setJsonData("dsp", value);
  }

  default @NotNull JSONObject getChartDynamicParameterFields() {
    return getDynamicParameterFields("chartDataSource");
  }

  default @NotNull JSONObject getValueDynamicParameterFields() {
    return getDynamicParameterFields("valueDataSource");
  }

  default @NotNull JSONObject getSetValueDynamicParameterFields() {
    return getDynamicParameterFields("setValueDataSource");
  }

  default @NotNull JSONObject getDynamicParameterFields(String key) {
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
    return new JSONObject();
  }
}
