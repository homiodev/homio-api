package org.touchhome.bundle.api.ui.field.selection.dynamic;

import org.json.JSONObject;

public interface HasDynamicParameterFields {
    void setDynamicParameterFieldsHolder(JSONObject value);

    JSONObject getDynamicParameterFieldsHolder();

    /**
     * Specify only in case when need distinguish DynamicParameterClass for different widget type and same entity
     */
    default DynamicRequestType getDynamicRequestType() {
        return null;
    }
}
