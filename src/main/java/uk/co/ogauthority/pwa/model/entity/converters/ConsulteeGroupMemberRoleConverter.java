package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class ConsulteeGroupMemberRoleConverter implements AttributeConverter<Set<ConsulteeGroupMemberRole>, String> {

  @Override
  public String convertToDatabaseColumn(Set<ConsulteeGroupMemberRole> consulteeGroupMemberRoles) {
    if (consulteeGroupMemberRoles == null) {
      return null;
    }

    return consulteeGroupMemberRoles.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<ConsulteeGroupMemberRole> convertToEntityAttribute(String csvRoleList) {
    return Arrays.stream(csvRoleList.split(","))
        .map(r -> EnumUtils.getEnumValue(ConsulteeGroupMemberRole.class, r))
        .collect(Collectors.toSet());
  }

}
