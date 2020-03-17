package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class PwaContactRoleConverter implements AttributeConverter<Set<PwaContactRole>, String> {

  @Override
  public String convertToDatabaseColumn(Set<PwaContactRole> pwaContactRoles) {
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
