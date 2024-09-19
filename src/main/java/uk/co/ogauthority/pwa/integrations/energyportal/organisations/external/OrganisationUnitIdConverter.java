package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;

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
