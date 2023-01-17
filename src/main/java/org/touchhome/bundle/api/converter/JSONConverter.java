package org.touchhome.bundle.api.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.touchhome.bundle.api.model.JSON;

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
