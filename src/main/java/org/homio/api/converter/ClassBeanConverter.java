package org.homio.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Converter(autoApply = true)
public class ClassBeanConverter implements AttributeConverter<Class, String> {

  @Override
  public String convertToDatabaseColumn(Class provider) {
    return provider == null ? null : provider.getName();
  }

  @Override
  public Class convertToEntityAttribute(String name) {
    try {
      return name == null ? null : Class.forName(name);
    } catch (Exception ex) {
      log.error("Unable to find class with name: {}", name);
      return null;
    }
  }
}
