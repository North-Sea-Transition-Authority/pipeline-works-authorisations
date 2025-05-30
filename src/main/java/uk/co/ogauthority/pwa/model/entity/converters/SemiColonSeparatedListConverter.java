package uk.co.ogauthority.pwa.model.entity.converters;

import io.micrometer.core.instrument.util.StringUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class SemiColonSeparatedListConverter implements AttributeConverter<List<String>, String> {
  private static final String DELIMITER = ";;;;";

  @Override
  public String convertToDatabaseColumn(List<String> stringSet) {
    if (stringSet == null || stringSet.isEmpty()) {
      return null;
    }

    return String.join(DELIMITER, stringSet);
  }

  @Override
  public List<String> convertToEntityAttribute(String delimitedString) {
    if (StringUtils.isEmpty(delimitedString)) {
      return Collections.emptyList();
    }
    return Arrays.stream(delimitedString.split(DELIMITER))
        .collect(Collectors.toList());
  }

}
