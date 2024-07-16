package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalCondition;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class EnvironmentalConditionConverter implements AttributeConverter<Set<EnvironmentalCondition>, String> {

  @Override
  public String convertToDatabaseColumn(Set<EnvironmentalCondition> environmentalConditions) {
    var converted = Optional.ofNullable(environmentalConditions)
        .stream()
        .flatMap(Collection::stream)
        .map(Enum::name)
        .collect(Collectors.joining(","));
    if (converted.isBlank()) {
      return null;
    }
    return converted;
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
