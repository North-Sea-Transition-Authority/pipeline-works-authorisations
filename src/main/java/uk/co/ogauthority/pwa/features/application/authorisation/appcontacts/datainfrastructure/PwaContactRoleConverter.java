package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.datainfrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class PwaContactRoleConverter implements AttributeConverter<Set<PwaContactRole>, String> {

  @Override
  public String convertToDatabaseColumn(Set<PwaContactRole> pwaContactRoles) {
    if (pwaContactRoles == null) {
      return null;
    }
    return pwaContactRoles.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<PwaContactRole> convertToEntityAttribute(String csvRoleList) {
    return Arrays.stream(csvRoleList.split(","))
        .map(r -> EnumUtils.getEnumValue(PwaContactRole.class, r))
        .collect(Collectors.toSet());
  }

}
