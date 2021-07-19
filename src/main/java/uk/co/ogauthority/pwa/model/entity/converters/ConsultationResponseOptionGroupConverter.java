package uk.co.ogauthority.pwa.model.entity.converters;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.util.EnumUtils;

@Converter
public class ConsultationResponseOptionGroupConverter implements AttributeConverter<Set<ConsultationResponseOptionGroup>, String> {

  @Override
  public String convertToDatabaseColumn(Set<ConsultationResponseOptionGroup> optionGroups) {
    if (optionGroups == null) {
      return null;
    }
    return optionGroups.stream()
        .map(Enum::name)
        .collect(Collectors.joining(","));
  }

  @Override
  public Set<ConsultationResponseOptionGroup> convertToEntityAttribute(String csvGroupList) {
    return Arrays.stream(csvGroupList.split(","))
        .map(r -> EnumUtils.getEnumValue(ConsultationResponseOptionGroup.class, r))
        .collect(Collectors.toSet());
  }

}
