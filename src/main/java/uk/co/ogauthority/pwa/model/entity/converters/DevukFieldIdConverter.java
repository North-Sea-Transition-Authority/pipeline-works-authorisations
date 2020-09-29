package uk.co.ogauthority.pwa.model.entity.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.energyportal.model.entity.devuk.DevukFieldId;

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

