package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;

public class DecommissioningConditionConverterTest {

  private DecommissioningConditionConverter converter;

  @Before
  public void setUp() {
    converter = new DecommissioningConditionConverter();
  }

  @Test
  public void testConvertToDatabaseColumn() {
    var result = converter.convertToDatabaseColumn(Set.of(
        DecommissioningCondition.EOL_REGULATION_STATEMENT,
        DecommissioningCondition.EOL_REMOVAL_STATEMENT
    ));
    assertThat(result).isEqualTo(String.format(
        "%s,%s",
        DecommissioningCondition.EOL_REGULATION_STATEMENT.name(),
        DecommissioningCondition.EOL_REMOVAL_STATEMENT.name()
    ));
  }

  @Test
  public void testConvertToEntityAttribute() {
    var result = converter.convertToEntityAttribute(String.format(
        "%s,%s",
        DecommissioningCondition.EOL_REGULATION_STATEMENT.name(),
        DecommissioningCondition.EOL_REMOVAL_STATEMENT.name()
    ));
    assertThat(result).containsExactlyInAnyOrder(
        DecommissioningCondition.EOL_REGULATION_STATEMENT,
        DecommissioningCondition.EOL_REMOVAL_STATEMENT
    );
  }
}