package uk.co.ogauthority.pwa.service.pwacontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.AssertionTestUtils;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaPermissionServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  private PwaPermissionService pwaPermissionService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  private MasterPwa masterPwa;

  private static int PWA_ORGANISATION_TEAM_ID = 1;

  private PortalOrganisationUnit organisationUnit1;


  @Before
  public void setUp() {

    pwaPermissionService = new PwaPermissionService(teamService, pwaConsentOrganisationRoleService);

    masterPwa = new MasterPwa();
    organisationUnit1 = PortalOrganisationTestUtils.getOrganisationUnit();
  }

  @Test
  public void getPwaPermissions_userInHolderTeam_userDoesNotHaveRegulatorRole() {

    var masterPwaHolderDto = new MasterPwaHolderDto(organisationUnit1, new PwaConsent());
    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of(masterPwaHolderDto));

    var pwaOrganisationTeam = new PwaOrganisationTeam(
        PWA_ORGANISATION_TEAM_ID, "", "", masterPwaHolderDto.getHolderOrganisationGroup().get());
    when(teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of(pwaOrganisationTeam));

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaPermission.VIEW_PWA);
  }


  @Test
  public void getPwaPermissions_userNotInHolderTeam_userHasRegulatorRole() {

    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of());

    when(teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of());

    int pwaRegulatorTeamId = 1;
    var regulatorTeam = new PwaRegulatorTeam(pwaRegulatorTeamId, "", "");
    when(teamService.getRegulatorTeamIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaRegulatorRole.class)))
        .thenReturn(Optional.of(regulatorTeam));

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    AssertionTestUtils.assertNotEmptyAndContains(permissions, PwaPermission.VIEW_PWA);

  }

  @Test
  public void getPwaPermissions_notHolderTeam_userDoesNotHaveRegulatorRole() {

    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa))
        .thenReturn(Set.of());

    when(teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of());

    when(teamService.getRegulatorTeamIfPersonInRole(user.getLinkedPerson(), EnumSet.allOf(PwaRegulatorRole.class)))
        .thenReturn(Optional.empty());

    var permissions = pwaPermissionService.getPwaPermissions(masterPwa, user);
    assertThat(permissions).isEmpty();
  }

}
