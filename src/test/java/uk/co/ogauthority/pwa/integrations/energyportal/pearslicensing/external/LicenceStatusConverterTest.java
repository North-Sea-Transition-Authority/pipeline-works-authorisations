package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.internal.LicenceStatusConverter;

class LicenceStatusConverterTest {

  private LicenceStatusConverter converter;

  @BeforeEach
  void setUp() {
    converter = new LicenceStatusConverter();
  }

  @Test
  void convertToDatabaseColumn_ProvidedValue() {
    LicenceStatus.stream()
        .forEach(licenceStatus -> {
          var result = converter.convertToDatabaseColumn(licenceStatus);
          assertThat(result).isEqualTo(licenceStatus.getInternalCharacter());
        });
  }

  @Test
  void convertToDatabaseColumn_WithNull() {
    var result = converter.convertToDatabaseColumn(null);
    assertThat(result).isNull();
  }

  @Test
  void convertToEntityAttribute_ProvidedValue() {
    LicenceStatus.stream()
        .forEach(licenceStatus -> {
          var result = converter.convertToEntityAttribute(licenceStatus.getInternalCharacter());
          assertThat(result).isEqualTo(licenceStatus);
        });
  }

  @Test
  void convertToEntityAttribute_WithNull() {
    var result = converter.convertToEntityAttribute(null);
    assertThat(result).isNull();
  }
}