package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal;

import java.util.Optional;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.LicenceStatus;

@Converter
public class LicenceStatusConverter implements AttributeConverter<LicenceStatus, String> {

  @Override
  public String convertToDatabaseColumn(LicenceStatus status) {
    var converted = Optional.ofNullable(status)
        .stream()
        .map(LicenceStatus::getInternalCharacter)
        .findAny()
        .orElse("");
    if (converted.isBlank()) {
      return null;
    }
    return converted;
  }

  @Override
  public LicenceStatus convertToEntityAttribute(String status) {
    if (StringUtils.isBlank(status)) {
      return null;
    }
    return LicenceStatus.stream()
        .filter(licenceStatus -> licenceStatus.getInternalCharacter().equals(status))
        .findAny()
        .orElse(null);
  }

}
