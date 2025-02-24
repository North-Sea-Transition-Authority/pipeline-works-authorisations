package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.EnvironmentalCondition;

class EnvironmentalConditionConverterTest {

  private EnvironmentalConditionConverter converter;

  @BeforeEach
  void setUp() {
    converter = new EnvironmentalConditionConverter();
  }

  @Test
  void convertToDatabaseColumnValidSet() {
    var result = converter.convertToDatabaseColumn(Set.of(
        EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE,
        EnvironmentalCondition.OPOL_LIABILITY_STATEMENT
    ));
    assertThat(result).contains(EnvironmentalCondition.DISCHARGE_FUNDS_AVAILABLE.name());
    assertThat(result).contains(EnvironmentalCondition.OPOL_LIABILITY_STATEMENT.name());
  }

  @Test
  void convertToDatabaseColumnEmptySet() {
    var result = converter.convertToDatabaseColumn(Set.of());
    assertThat(result).isNull();
  }

  @Test
  void convertToDatabaseColumnNullSet() {
    var result = converter.convertToDatabaseColumn(null);
    assertThat(result).isNull();
  }

  @Test
  void convertToEntityAttributeValidConditionString() {
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
  void convertToEntityAttributeEmptyConditionsString() {
    var result = converter.convertToEntityAttribute("");
    assertThat(result).isEqualTo(Set.of());
  }

  @Test
  void convertToEntityAttributeNullConditionsString() {
    var result = converter.convertToEntityAttribute(null);
    assertThat(result).isEqualTo(Set.of());
  }

}