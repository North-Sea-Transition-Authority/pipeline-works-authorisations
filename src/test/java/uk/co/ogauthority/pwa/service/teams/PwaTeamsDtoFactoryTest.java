package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;

@ExtendWith(MockitoExtension.class)
class PwaTeamsDtoFactoryTest {

  private PwaTeamsDtoFactory pwaTeamsDtoFactory = new PwaTeamsDtoFactory();


  @Test
  void createPwaUserPrivilegeSet_mapsPrivAsExpected_andRemovesDuplicates() {
    Set<PwaUserPrivilege> privs = pwaTeamsDtoFactory.createPwaUserPrivilegeSet(List.of(
        new PortalSystemPrivilegeDto(PwaTeamType.REGULATOR.getPortalTeamType(), "SomeRole",
            PwaUserPrivilege.PWA_WORKAREA.name()),
        new PortalSystemPrivilegeDto(PwaTeamType.ORGANISATION.getPortalTeamType(), "SomeOtherRole",
            PwaUserPrivilege.PWA_WORKAREA.name()),
        new PortalSystemPrivilegeDto(PwaTeamType.ORGANISATION.getPortalTeamType(), "DifferentRole",
            "unknown priv")
    ));

    assertThat(privs).hasSize(1);
    assertThat(privs).containsExactly(PwaUserPrivilege.PWA_WORKAREA);
  }

}
