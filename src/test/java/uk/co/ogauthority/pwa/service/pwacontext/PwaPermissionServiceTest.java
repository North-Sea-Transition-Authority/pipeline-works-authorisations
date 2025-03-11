package uk.co.ogauthority.pwa.service.pwacontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class PwaPermissionServiceTest {

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @InjectMocks
  private PwaPermissionService pwaPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  private MasterPwa masterPwa;


  @BeforeEach
  void setUp() {

    masterPwa = new MasterPwa();
  }

  @Test
  void getPwaPermissions_userInHolderTeam() {

    when(pwaHolderTeamService.isPersonInHolderTeam(masterPwa, user.getLinkedPerson()))
        .thenReturn(true);

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).containsExactlyInAnyOrder(
        PwaPermission.VIEW_PWA,
        PwaPermission.VIEW_PWA_PIPELINE,
        PwaPermission.SHOW_PWA_NAVIGATION
    );
  }

  @Test
  void getPwaPermissions_userNotInHolderTeamOrRegulatorTeam_hasViewPipelinePermission() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PIPELINE_VIEW));

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).containsExactlyInAnyOrder(PwaPermission.VIEW_PWA_PIPELINE);
  }


  @Test
  void getPwaPermissions_userNotInHolderTeam_userIsRegulator() {

    when(pwaHolderTeamService.isPersonInHolderTeam(masterPwa, user.getLinkedPerson()))
        .thenReturn(false);

    int pwaRegulatorTeamId = 1;
    var regulatorTeam = new PwaRegulatorTeam(pwaRegulatorTeamId, "", "");
    when(teamQueryService.userIsMemberOfStaticTeam((long) user.getWuaId(), TeamType.REGULATOR))
        .thenReturn(true);

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).containsExactlyInAnyOrder(
        PwaPermission.VIEW_PWA,
        PwaPermission.VIEW_PWA_PIPELINE,
        PwaPermission.SHOW_PWA_NAVIGATION
    );
  }

  @Test
  void getPwaPermissions_notHolderTeam_userDoesNotHaveRegulatorRole() {

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).isEmpty();
  }

}
