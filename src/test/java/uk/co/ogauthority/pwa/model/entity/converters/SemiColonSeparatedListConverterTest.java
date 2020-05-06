package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SemiColonSeparatedListConverterTest {

  private SemiColonSeparatedListConverter semiColonSeparatedListConverter;

  @Before
  public void setup() {

    semiColonSeparatedListConverter = new SemiColonSeparatedListConverter();

  }


  @Test
  public void convertToDatabaseColumn_whenNull() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
  }

  @Test
  public void convertToDatabaseColumn_whenEmptySet() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(List.of());
    assertThat(result).isNull();
  }

  @Test
  public void convertToDatabaseColumn_whenPopulatedSet() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(List.of("ONE", "TWO", "THREE"));
    assertThat(result).isEqualTo("ONE;;;;TWO;;;;THREE");
  }


  @Test
  public void convertToEntityAttribute_whenNull() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute(null);
    assertThat(result).isEmpty();
  }

  @Test
  public void convertToEntityAttribute_whenEmptyString() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute("");
    assertThat(result).isEmpty();
  }

  @Test
  public void convertToEntityAttribute_whenDelimitedString() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute("ONE;;;;TWO;;;;THREE");
    assertThat(result).containsExactly("ONE", "TWO", "THREE");
  }


}