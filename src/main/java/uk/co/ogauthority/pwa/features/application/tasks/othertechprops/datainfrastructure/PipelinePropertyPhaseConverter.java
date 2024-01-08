package uk.co.ogauthority.pwa.features.application.tasks.othertechprops.datainfrastructure;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyPhase;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class PipelinePropertyPhaseConverter implements AttributeConverter<Set<PropertyPhase>, String> {

  @Override
  public String convertToDatabaseColumn(Set<PropertyPhase> propertyPhases) {
    propertyPhases = propertyPhases != null ? propertyPhases : Set.of();
    return propertyPhases.stream()
        .filter(Objects::nonNull)
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<PropertyPhase> convertToEntityAttribute(String csvPropertyPhases) {
    var phasesStrList = StringUtils.isBlank(csvPropertyPhases) ? new String[0] : csvPropertyPhases.split(",");
    return Arrays.stream(phasesStrList)
        .map(r -> EnumUtils.getEnumValue(PropertyPhase.class, r))
        .collect(Collectors.toSet());
  }

}
