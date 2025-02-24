package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SemiColonSeparatedListConverterTest {

  private SemiColonSeparatedListConverter semiColonSeparatedListConverter;

  @BeforeEach
  void setup() {

    semiColonSeparatedListConverter = new SemiColonSeparatedListConverter();

  }


  @Test
  void convertToDatabaseColumn_whenNull() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(null);
        assertThat(result).isNull();
  }

  @Test
  void convertToDatabaseColumn_whenEmptySet() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(List.of());
    assertThat(result).isNull();
  }

  @Test
  void convertToDatabaseColumn_whenPopulatedSet() {
    var result = semiColonSeparatedListConverter.convertToDatabaseColumn(List.of("ONE", "TWO", "THREE"));
    assertThat(result).isEqualTo("ONE;;;;TWO;;;;THREE");
  }


  @Test
  void convertToEntityAttribute_whenNull() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute(null);
    assertThat(result).isEmpty();
  }

  @Test
  void convertToEntityAttribute_whenEmptyString() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute("");
    assertThat(result).isEmpty();
  }

  @Test
  void convertToEntityAttribute_whenDelimitedString() {
    var result = semiColonSeparatedListConverter.convertToEntityAttribute("ONE;;;;TWO;;;;THREE");
    assertThat(result).containsExactly("ONE", "TWO", "THREE");
  }


}