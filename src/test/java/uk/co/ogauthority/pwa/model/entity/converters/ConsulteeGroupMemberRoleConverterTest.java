package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;

@RunWith(SpringRunner.class)
public class ConsulteeGroupMemberRoleConverterTest {

  private final ConsulteeGroupMemberRoleConverter converter = new ConsulteeGroupMemberRoleConverter();

  @Test
  public void convertToDatabaseColumn() {

    String csvRoleList = converter.convertToDatabaseColumn(Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    try {
      assertThat(csvRoleList).isEqualTo("ACCESS_MANAGER");
    } catch (AssertionError e) {
      assertThat(csvRoleList).isEqualTo("ACCESS_MANAGER");
    }

  }

  @Test
  public void convertToEntityAttribute() {

    Set<ConsulteeGroupMemberRole> roles = converter.convertToEntityAttribute("RECIPIENT,RESPONDER");

    assertThat(roles).containsExactlyInAnyOrder(ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER);

  }

}
