package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.datainfrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom.DecommissioningCondition;

class DecommissioningConditionConverterTest {

  private DecommissioningConditionConverter converter;

  @BeforeEach
  void setUp() {
    converter = new DecommissioningConditionConverter();
  }

  @Test
  void convertToDatabaseColumnValidSet() {
    var decommissioningConditions = Set.of(
        DecommissioningCondition.EOL_REGULATION_STATEMENT,
        DecommissioningCondition.EOL_REMOVAL_STATEMENT,
        DecommissioningCondition.EOL_REMOVAL_PROPOSAL,
        DecommissioningCondition.EOL_BUNDLES_STATEMENT
    );
    var result = converter.convertToDatabaseColumn(decommissioningConditions);

    assertTrue(Arrays.asList(result.split(","))
        .containsAll(decommissioningConditions.stream()
            .map(DecommissioningCondition::name)
            .collect(Collectors.toSet())
        )
    );
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
  void convertToEntityAttributeValidConditionsString() {
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