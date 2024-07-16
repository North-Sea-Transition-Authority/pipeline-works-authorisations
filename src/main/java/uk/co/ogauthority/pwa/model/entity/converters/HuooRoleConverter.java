package uk.co.ogauthority.pwa.model.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class HuooRoleConverter implements AttributeConverter<Set<HuooRole>, String> {
  @Override
  public String convertToDatabaseColumn(Set<HuooRole> attribute) {
    return Optional.ofNullable(attribute).stream()
        .flatMap(Collection::stream)
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<HuooRole> convertToEntityAttribute(String dbData) {
    if (StringUtils.isBlank(dbData)) {
      return Set.of();
    }
    return Arrays.stream(dbData.split(","))
        .map(r -> EnumUtils.getEnumValue(HuooRole.class, r))
        .collect(Collectors.toSet());
  }
}
