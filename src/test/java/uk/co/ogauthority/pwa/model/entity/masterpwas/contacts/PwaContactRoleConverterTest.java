package uk.co.ogauthority.pwa.model.entity.masterpwas.contacts;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.converters.PwaContactRoleConverter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

@RunWith(SpringRunner.class)
public class PwaContactRoleConverterTest {

  private PwaContactRoleConverter converter = new PwaContactRoleConverter();

  @Test
  public void convertToDatabaseColumn_whenNotNull() {

    String csvRoleList = converter.convertToDatabaseColumn(Set.of(PwaContactRole.ACCESS_MANAGER));

    assertThat(csvRoleList).isEqualTo("ACCESS_MANAGER");

  }

  @Test
  public void convertToDatabaseColumn_whenNull() {

    assertThat(converter.convertToDatabaseColumn(null)).isNull();

  }

  @Test
  public void convertToEntityAttribute() {

    Set<PwaContactRole> roles = converter.convertToEntityAttribute("PREPARER,VIEWER");

    assertThat(roles).containsExactlyInAnyOrder(PwaContactRole.PREPARER, PwaContactRole.VIEWER);

  }

}
