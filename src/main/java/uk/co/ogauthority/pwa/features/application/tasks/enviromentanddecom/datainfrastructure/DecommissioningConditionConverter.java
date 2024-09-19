package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.DecommissioningCondition;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class DecommissioningConditionConverter implements AttributeConverter<Set<DecommissioningCondition>, String> {

  @Override
  public String convertToDatabaseColumn(Set<DecommissioningCondition> decommissioningConditions) {
    var converted = Optional.ofNullable(decommissioningConditions)
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
  public Set<DecommissioningCondition> convertToEntityAttribute(String conditions) {
    if (StringUtils.isBlank(conditions)) {
      return Set.of();
    }
    return Arrays.stream(conditions.split(","))
        .map(r -> EnumUtils.getEnumValue(DecommissioningCondition.class, r))
        .collect(Collectors.toSet());
  }

}
