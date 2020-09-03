package uk.co.ogauthority.pwa.model.entity.converters;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

@RunWith(MockitoJUnitRunner.class)
public class PersonIdConverterTest {
  private static final int PERSON_ID = 1;
  private static final PersonId PERSON_ID_OBJ = new PersonId(PERSON_ID);
  private PersonIdConverter personIdConverter;

  @Before
  public void setup(){
    personIdConverter = new PersonIdConverter();
  }

  @Test
  public void convertToDatabaseColumn_whenNotNull() {

    assertThat(personIdConverter.convertToDatabaseColumn(PERSON_ID_OBJ)).isEqualTo(PERSON_ID);
  }

  @Test
  public void convertToDatabaseColumn_whenNull() {

    assertThat(personIdConverter.convertToDatabaseColumn(null)).isNull();
  }

  @Test
  public void convertToEntityAttribute_whenNotNull() {
    assertThat(personIdConverter.convertToEntityAttribute(PERSON_ID)).isEqualTo(PERSON_ID_OBJ);
  }

  @Test
  public void convertToEntityAttribute_whenNull() {
    assertThat(personIdConverter.convertToEntityAttribute(null)).isNull();
  }
}