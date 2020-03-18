package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class DecommissioningConditionConverter implements AttributeConverter<Set<DecommissioningCondition>, String> {

  @Override
  public String convertToDatabaseColumn(Set<DecommissioningCondition> decommissioningConditions) {
    if (decommissioningConditions.isEmpty()) {
      return null;
    }
    return decommissioningConditions
        .stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<DecommissioningCondition> convertToEntityAttribute(String conditions) {
    if (StringUtils.isBlank(conditions)) {
      return Set.of();
    }
    return Arrays.stream(conditions.split(","))
        .map(r -> EnumUtils.getEnumValue(DecommissioningCondition.class, r))
        .collect(Collectors.toSet());
  }

}
