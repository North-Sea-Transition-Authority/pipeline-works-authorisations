package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLicenceStatus;

public class BlockLicenceStatusConverterTest {

  private BlockLicenceStatusConverter converter;

  @Before
  public void setUp() {
    converter = new BlockLicenceStatusConverter();
  }

  @Test
  public void convertToDatabaseColumn_ProvidedValue() {
    BlockLicenceStatus.stream()
        .forEach(licenceStatus -> {
          var result = converter.convertToDatabaseColumn(licenceStatus);
          assertThat(result).isEqualTo(licenceStatus.getInternalCharacter());
        });
  }

  @Test
  public void convertToDatabaseColumn_WithNull() {
    var result = converter.convertToDatabaseColumn(null);
    assertThat(result).isNull();
  }

  @Test
  public void convertToEntityAttribute_ProvidedValue() {
    BlockLicenceStatus.stream()
        .forEach(licenceStatus -> {
          var result = converter.convertToEntityAttribute(licenceStatus.getInternalCharacter());
          assertThat(result).isEqualTo(licenceStatus);
        });
  }

  @Test
  public void convertToEntityAttribute_WithNull() {
    var result = converter.convertToEntityAttribute(null);
    assertThat(result).isNull();
  }
}