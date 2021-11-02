package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.internal;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldId;

@Converter
public class DevukFieldIdConverter implements AttributeConverter<DevukFieldId, Integer> {

  @Override
  public Integer convertToDatabaseColumn(DevukFieldId attribute) {
    return attribute != null ? attribute.asInt() : null;
  }

  @Override
  public DevukFieldId convertToEntityAttribute(Integer dbData) {
    return dbData != null ? new DevukFieldId(dbData) : null;
  }
}

