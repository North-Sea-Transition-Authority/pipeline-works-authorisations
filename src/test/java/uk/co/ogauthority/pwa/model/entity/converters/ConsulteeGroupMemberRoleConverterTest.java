package uk.co.ogauthority.pwa.model.entity.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;

@RunWith(SpringRunner.class)
class ConsulteeGroupMemberRoleConverterTest {

  private final ConsulteeGroupMemberRoleConverter converter = new ConsulteeGroupMemberRoleConverter();

  @Test
  void convertToDatabaseColumn_whenNotNull() {

    String csvRoleList = converter.convertToDatabaseColumn(Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    assertThat(csvRoleList).isEqualTo("ACCESS_MANAGER");


  }

  @Test
  void convertToDatabaseColumn_whenNull() {

    assertThat(converter.convertToDatabaseColumn(null)).isNull();

  }

  @Test
  void convertToEntityAttribute() {

    Set<ConsulteeGroupMemberRole> roles = converter.convertToEntityAttribute("RECIPIENT,RESPONDER");

    assertThat(roles).containsExactlyInAnyOrder(ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER);

  }

}
