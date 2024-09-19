package uk.co.ogauthority.pwa.model.entity.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;

@Converter
public class PipelineDetailIdConverter implements AttributeConverter<PipelineDetailId, Integer> {

  @Override
  public Integer convertToDatabaseColumn(PipelineDetailId attribute) {
    return attribute != null ? attribute.asInt() : null;
  }

  @Override
  public PipelineDetailId convertToEntityAttribute(Integer dbData) {
    return dbData != null ?  new PipelineDetailId(dbData) : null;
  }
}
