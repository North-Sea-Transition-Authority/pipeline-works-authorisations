package uk.co.ogauthority.pwa.service.pwacontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@RunWith(MockitoJUnitRunner.class)
public class PwaPermissionServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  private PwaPermissionService pwaPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  private MasterPwa masterPwa;


  @Before
  public void setUp() {

    pwaPermissionService = new PwaPermissionService(teamService, pwaHolderTeamService);

    masterPwa = new MasterPwa();
  }

  @Test
  public void getPwaPermissions_userInHolderTeam() {

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
  public void getPwaPermissions_userNotInHolderTeamOrRegulatorTeam_hasViewPipelinePermission() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PIPELINE_VIEW));

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).containsExactlyInAnyOrder(PwaPermission.VIEW_PWA_PIPELINE);
  }


  @Test
  public void getPwaPermissions_userNotInHolderTeam_userHasRegulatorRole() {

    when(pwaHolderTeamService.isPersonInHolderTeam(masterPwa, user.getLinkedPerson()))
        .thenReturn(false);

    int pwaRegulatorTeamId = 1;
    var regulatorTeam = new PwaRegulatorTeam(pwaRegulatorTeamId, "", "");
    when(teamService.getRegulatorTeamIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaRegulatorRole.class)))
        .thenReturn(Optional.of(regulatorTeam));

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).containsExactlyInAnyOrder(
        PwaPermission.VIEW_PWA,
        PwaPermission.VIEW_PWA_PIPELINE,
        PwaPermission.SHOW_PWA_NAVIGATION
    );
  }

  @Test
  public void getPwaPermissions_notHolderTeam_userDoesNotHaveRegulatorRole() {

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).isEmpty();
  }

}
