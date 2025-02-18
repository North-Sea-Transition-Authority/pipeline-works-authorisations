package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.datainfrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;

@RunWith(SpringRunner.class)
class PwaContactRoleConverterTest {

  private PwaContactRoleConverter converter = new PwaContactRoleConverter();

  @Test
  void convertToDatabaseColumn_whenNotNull() {

    String csvRoleList = converter.convertToDatabaseColumn(Set.of(PwaContactRole.ACCESS_MANAGER));

    assertThat(csvRoleList).isEqualTo("ACCESS_MANAGER");

  }

  @Test
  void convertToDatabaseColumn_whenNull() {

    assertThat(converter.convertToDatabaseColumn(null)).isNull();

  }

  @Test
  void convertToEntityAttribute() {

    Set<PwaContactRole> roles = converter.convertToEntityAttribute("PREPARER,VIEWER");

    assertThat(roles).containsExactlyInAnyOrder(PwaContactRole.PREPARER, PwaContactRole.VIEWER);

  }

}
