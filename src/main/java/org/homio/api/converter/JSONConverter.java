package org.homio.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.homio.api.model.JSON;

@Converter(autoApply = true)
public class JSONConverter implements AttributeConverter<JSON, String> {
    @Override
    public String convertToDatabaseColumn(JSON jsonData) {
        String json;
        try {
            json = jsonData.toString();
        } catch (Exception ex) {
            json = "";
        }
        return json;
    }

    @Override
    public JSON convertToEntityAttribute(String jsonDataAsJson) {
        try {
            return new JSON(jsonDataAsJson);
        } catch (Exception ex) {
            return new JSON();
        }
    }
}
