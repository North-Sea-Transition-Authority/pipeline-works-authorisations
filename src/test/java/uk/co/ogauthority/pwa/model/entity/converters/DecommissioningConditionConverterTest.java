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
  public void testConvertToDatabaseColumn_ValidSet() {
    var result = converter.convertToDatabaseColumn(Set.of(
        DecommissioningCondition.EOL_REGULATION_STATEMENT,
        DecommissioningCondition.EOL_REMOVAL_STATEMENT
    ));
    assertThat(result).contains(DecommissioningCondition.EOL_REGULATION_STATEMENT.name());
    assertThat(result).contains(DecommissioningCondition.EOL_REMOVAL_STATEMENT.name());
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
  public void testConvertToEntityAttribute_ValidConditionsString() {
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