package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class DecommissioningConditionConverter implements AttributeConverter<Set<DecommissioningCondition>, String> {

  @Override
  public String convertToDatabaseColumn(Set<DecommissioningCondition> pwaContactRoles) {
    return pwaContactRoles.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<DecommissioningCondition> convertToEntityAttribute(String csvRoleList) {
    return Arrays.stream(csvRoleList.split(","))
        .map(r -> EnumUtils.getEnumValue(DecommissioningCondition.class, r))
        .collect(Collectors.toSet());
  }

}
