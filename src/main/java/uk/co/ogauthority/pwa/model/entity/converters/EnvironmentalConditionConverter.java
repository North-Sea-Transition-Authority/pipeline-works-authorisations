package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class EnvironmentalConditionConverter implements AttributeConverter<Set<EnvironmentalCondition>, String> {

  @Override
  public String convertToDatabaseColumn(Set<EnvironmentalCondition> environmentalConditions) {
    if (environmentalConditions.isEmpty()) {
      return null;
    }
    return environmentalConditions
        .stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<EnvironmentalCondition> convertToEntityAttribute(String conditions) {
    if (StringUtils.isBlank(conditions)) {
      return Set.of();
    }
    return Arrays.stream(conditions.split(","))
        .map(r -> EnumUtils.getEnumValue(EnvironmentalCondition.class, r))
        .collect(Collectors.toSet());
  }

}
