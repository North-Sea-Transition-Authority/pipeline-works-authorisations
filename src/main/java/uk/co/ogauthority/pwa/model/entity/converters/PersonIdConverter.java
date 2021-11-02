package uk.co.ogauthority.pwa.model.entity.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

@Converter
public class PersonIdConverter implements AttributeConverter<PersonId, Integer> {

  @Override
  public Integer convertToDatabaseColumn(PersonId attribute) {
    return attribute != null ? attribute.asInt() : null;
  }

  @Override
  public PersonId convertToEntityAttribute(Integer dbData) {
    return dbData != null ?  new PersonId(dbData) : null;
  }
}
