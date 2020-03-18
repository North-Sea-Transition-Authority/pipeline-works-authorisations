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
  public void testConvertToDatabaseColumn() {
    var result = converter.convertToDatabaseColumn(Set.of(
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE,
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT
    ));
    assertThat(result).isEqualTo(String.format(
        "%s,%s",
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE.name(),
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT.name()
    ));
  }

  @Test
  public void testConvertToEntityAttribute() {
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

}