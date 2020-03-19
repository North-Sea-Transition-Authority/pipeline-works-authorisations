package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;

public class EnvironmentalConditionConverterTest {

  private EnvironmentalConditionConverter converter;

  @Before
  public void setUp() {
    converter = new EnvironmentalConditionConverter();
  }

  @Test
  public void testConvertToDatabaseColumn_ValidSet() {
    var result = converter.convertToDatabaseColumn(Set.of(
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE,
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT
    ));
    assertThat(result).contains(EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE.name());
    assertThat(result).contains(EnvironmentalCondition.OPOL_LIABILITY_STATEMENT.name());
  }

  @Test
  public void testConvertToDatabaseColumn_EmptySet() {
    var result = converter.convertToDatabaseColumn(Set.of());
    assertThat(result).isNull();
  }

  @Test
  public void testConvertToDatabaseColumn_NullSet() {
    var result = converter.convertToDatabaseColumn(null);
    assertThat(result).isNull();
  }

  @Test
  public void testConvertToEntityAttribute_ValidConditionString() {
    var result = converter.convertToEntityAttribute(String.format(
        "%s,%s",
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE.name(),
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT.name()
    ));
    assertThat(result).containsExactlyInAnyOrder(
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE,
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT
    );
  }

  @Test
  public void testConvertToEntityAttribute_EmptyConditionsString() {
    var result = converter.convertToEntityAttribute("");
    assertThat(result).isEqualTo(Set.of());
  }

  @Test
  public void testConvertToEntityAttribute_NullConditionsString() {
    var result = converter.convertToEntityAttribute(null);
    assertThat(result).isEqualTo(Set.of());
  }

}