package org.homio.api.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.homio.api.entity.HasJsonData.LIST_DELIMITER;

@Converter
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

  @Override
  public String convertToDatabaseColumn(Set<String> set) {
    return set == null ? "" : String.join(LIST_DELIMITER, set);
  }

  @Override
  public Set<String> convertToEntityAttribute(String data) {
    return StringUtils.isEmpty(data) ? new HashSet<>() : new HashSet<>(Arrays.asList(data.split(LIST_DELIMITER)));
  }
}
