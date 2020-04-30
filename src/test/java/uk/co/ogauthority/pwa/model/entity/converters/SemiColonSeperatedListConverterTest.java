package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SemiColonSeperatedListConverterTest {

  private SemiColonSeperatedListConverter semiColonSeperatedListConverter;

  @Before
  public void setup() {

    semiColonSeperatedListConverter = new SemiColonSeperatedListConverter();

  }


  @Test
  public void convertToDatabaseColumn_whenNull() {
    var result = semiColonSeperatedListConverter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
  }

  @Test
  public void convertToDatabaseColumn_whenEmptySet() {
    var result = semiColonSeperatedListConverter.convertToDatabaseColumn(List.of());
    assertThat(result).isNull();
  }

  @Test
  public void convertToDatabaseColumn_whenPopulatedSet() {
    var result = semiColonSeperatedListConverter.convertToDatabaseColumn(List.of("ONE", "TWO", "THREE"));
    assertThat(result).isEqualTo("ONE;;;;TWO;;;;THREE");
  }


  @Test
  public void convertToEntityAttribute_whenNull() {
    var result = semiColonSeperatedListConverter.convertToEntityAttribute(null);
    assertThat(result).isEmpty();
  }

  @Test
  public void convertToEntityAttribute_whenEmptyString() {
    var result = semiColonSeperatedListConverter.convertToEntityAttribute("");
    assertThat(result).isEmpty();
  }

  @Test
  public void convertToEntityAttribute_whenDelimitedString() {
    var result = semiColonSeperatedListConverter.convertToEntityAttribute("ONE;;;;TWO;;;;THREE");
    assertThat(result).containsExactly("ONE", "TWO", "THREE");
  }


}