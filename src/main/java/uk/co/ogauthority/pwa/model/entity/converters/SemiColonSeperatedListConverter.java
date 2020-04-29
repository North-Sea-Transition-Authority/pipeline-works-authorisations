package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class SemiColonSeperatedListConverter implements AttributeConverter<Set<String>, String> {
  private static final String DELIMITER = ";;;;";

  @Override
  public String convertToDatabaseColumn(Set<String> pwaContactRoles) {
    return String.join(DELIMITER, pwaContactRoles);
  }

  @Override
  public Set<String> convertToEntityAttribute(String csvRoleList) {
    if (csvRoleList == null) {
      return Collections.emptySet();
    }
    return Arrays.stream(csvRoleList.split(DELIMITER))
        .collect(Collectors.toSet());
  }

}
