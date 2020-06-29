package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class PipelinePropertyPhaseConverter implements AttributeConverter<Set<PropertyPhase>, String> {

  @Override
  public String convertToDatabaseColumn(Set<PropertyPhase> propertyPhases) {
    return propertyPhases.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<PropertyPhase> convertToEntityAttribute(String csvPropertyPhaseList) {
    return Arrays.stream(csvPropertyPhaseList.split(","))
        .map(r -> EnumUtils.getEnumValue(PropertyPhase.class, r))
        .collect(Collectors.toSet());
  }

}
