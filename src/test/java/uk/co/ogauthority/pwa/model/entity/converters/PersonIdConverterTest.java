package uk.co.ogauthority.pwa.model.entity.converters;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;

@ExtendWith(MockitoExtension.class)
class PersonIdConverterTest {
  private static final int PERSON_ID = 1;
  private static final PersonId PERSON_ID_OBJ = new PersonId(PERSON_ID);
  private PersonIdConverter personIdConverter;

  @BeforeEach
  void setup(){
    personIdConverter = new PersonIdConverter();
  }

  @Test
  void convertToDatabaseColumn_whenNotNull() {

    assertThat(personIdConverter.convertToDatabaseColumn(PERSON_ID_OBJ)).isEqualTo(PERSON_ID);
  }

  @Test
  void convertToDatabaseColumn_whenNull() {

    assertThat(personIdConverter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  void convertToEntityAttribute_whenNotNull() {
    assertThat(personIdConverter.convertToEntityAttribute(PERSON_ID)).isEqualTo(PERSON_ID_OBJ);
  }

  @Test
  void convertToEntityAttribute_whenNull() {
    assertThat(personIdConverter.convertToEntityAttribute(null)).isNull();
  }
}