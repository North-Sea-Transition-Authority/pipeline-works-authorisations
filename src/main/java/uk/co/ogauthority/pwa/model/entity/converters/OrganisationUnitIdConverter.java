package uk.co.ogauthority.pwa.model.entity.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;

@Converter
public class OrganisationUnitIdConverter implements AttributeConverter<OrganisationUnitId, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrganisationUnitId attribute) {
    return attribute != null ? attribute.asInt() : null;
  }

  @Override
  public OrganisationUnitId convertToEntityAttribute(Integer dbData) {
    return dbData != null ?  new OrganisationUnitId(dbData) : null;
  }

}
