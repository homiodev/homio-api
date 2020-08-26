package org.touchhome.bundle.api.converter;

import org.json.JSONObject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {
    @Override
    public String convertToDatabaseColumn(JSONObject jsonData) {
        String json;
        try {
            json = jsonData.toString();
        } catch (Exception ex) {
            json = "";
        }
        return json;
    }

    @Override
    public JSONObject convertToEntityAttribute(String jsonDataAsJson) {
        JSONObject jsonData;
        try {
            jsonData = new JSONObject(jsonDataAsJson);
        } catch (Exception ex) {
            jsonData = new JSONObject();
        }
        return jsonData;
    }
}
