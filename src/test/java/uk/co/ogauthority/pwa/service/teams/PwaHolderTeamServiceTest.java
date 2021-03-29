package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.model.teammanagement.TeamRoleView;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaconsents.MasterPwaHolderDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentOrganisationRoleService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaHolderTeamServiceTest {

  @Mock
  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private TeamManagementService teamManagementService;

  private PwaHolderTeamService pwaHolderTeamService;

  private PortalOrganisationGroup consentedHolderGroup, appHolderGroup;
  private PortalOrganisationUnit consentedHolderOu, appHolderOu;
  private PwaOrganisationTeam consentedHolderTeam, appHolderTeam;

  private PadOrganisationRole appHolderOrgRole;

  private PwaApplicationDetail detail;

  private Person person;

  private Set<PwaOrganisationRole> teamMemberOrgRoleSet, appMemberOrgRoleSet;
  private Set<PwaRole> teamMemberRoleSet, appMemberRoleSet;
  private PwaTeamMember teamMember, appMember;
  private TeamMemberView teamMemberView, appMemberView;

  @Before
  public void setUp() {

    person = PersonTestUtil.createDefaultPerson();

    pwaHolderTeamService = new PwaHolderTeamService(pwaConsentOrganisationRoleService, padOrganisationRoleService, teamService, teamManagementService,
        portalOrganisationsAccessor);

    consentedHolderGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "O", "O");
    appHolderGroup = PortalOrganisationTestUtils.generateOrganisationGroup(2, "T", "T");

    consentedHolderOu = PortalOrganisationTestUtils.generateOrganisationUnit(1, "OO", consentedHolderGroup);
    appHolderOu = PortalOrganisationTestUtils.generateOrganisationUnit(2, "TT", appHolderGroup);

    consentedHolderTeam = new PwaOrganisationTeam(1, "1", "1", consentedHolderGroup);

    appHolderTeam = new PwaOrganisationTeam(1, "team", "d", appHolderGroup);
    when(teamService.getOrganisationTeamListIfPersonInRole(eq(person), any())).thenReturn(List.of(appHolderTeam));

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of(consentedHolderTeam, appHolderTeam));
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(consentedHolderTeam, appHolderTeam));

    detail = new PwaApplicationDetail();
    var application = new PwaApplication();
    detail.setPwaApplication(application);

    appHolderOrgRole = new PadOrganisationRole(HuooRole.HOLDER);
    appHolderOrgRole.setPwaApplicationDetail(detail);
    appHolderOrgRole.setType(HuooType.PORTAL_ORG);
    appHolderOrgRole.setOrganisationUnit(appHolderOu);
    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(detail, HuooRole.HOLDER)).thenReturn(List.of(appHolderOrgRole));

    teamMemberOrgRoleSet = Set.of(PwaOrganisationRole.TEAM_ADMINISTRATOR, PwaOrganisationRole.APPLICATION_SUBMITTER);
    teamMemberRoleSet = Set.of(
        TeamTestingUtils.generatePwaRole(PwaOrganisationRole.TEAM_ADMINISTRATOR.getPortalTeamRoleName(), 10),
        TeamTestingUtils.generatePwaRole(PwaOrganisationRole.APPLICATION_SUBMITTER.getPortalTeamRoleName(), 20)
    );

    var teamMemberRoleViews = teamMemberRoleSet.stream()
        .map(r -> new TeamRoleView(r.getName(), null, null, r.getDisplaySequence()))
        .collect(Collectors.toSet());

    appMemberOrgRoleSet = Set.of(PwaOrganisationRole.TEAM_ADMINISTRATOR, PwaOrganisationRole.APPLICATION_CREATOR);
    appMemberRoleSet = Set.of(
        TeamTestingUtils.generatePwaRole(PwaOrganisationRole.TEAM_ADMINISTRATOR.getPortalTeamRoleName(), 10),
        TeamTestingUtils.generatePwaRole(PwaOrganisationRole.APPLICATION_CREATOR.getPortalTeamRoleName(), 20)
    );

    var appMemberRoleViews = appMemberRoleSet.stream()
        .map(r -> new TeamRoleView(r.getName(), null, null, r.getDisplaySequence()))
        .collect(Collectors.toSet());

    teamMember = new PwaTeamMember(consentedHolderTeam, person, teamMemberRoleSet);
    appMember = new PwaTeamMember(appHolderTeam, person, appMemberRoleSet);

    teamMemberView = new TeamMemberView(person, null, null, teamMemberRoleViews);
    appMemberView = new TeamMemberView(person, null, null, appMemberRoleViews);

    when(teamService.getTeamMembers(appHolderTeam)).thenReturn(List.of(appMember));

  }

  @Test
  public void isPersonInHolderTeam_consentedHolderExists() {

    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(any())).thenReturn(Set.of(
        new MasterPwaHolderDto(consentedHolderOu, null)));

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail, person);

    verifyNoInteractions(padOrganisationRoleService);

    assertThat(inTeam).isTrue();

  }

  @Test
  public void isPersonInHolderTeam_noConsented_appHolderUsed() {

    // don't set up consented holder so that we fall back to app holders

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail, person);

    verify(pwaConsentOrganisationRoleService, times(1)).getCurrentHoldersOrgRolesForMasterPwa(any());
    verify(padOrganisationRoleService, times(1)).getAssignableOrgRolesForDetailByRole(detail, HuooRole.HOLDER);

    assertThat(inTeam).isTrue();

  }

  @Test
  public void getRolesInHolderTeam_consentedHolderExists() {

    when(teamManagementService.getTeamMemberViewForTeamAndPerson(consentedHolderTeam, person))
        .thenReturn(Optional.of(teamMemberView));

    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(any())).thenReturn(Set.of(
        new MasterPwaHolderDto(consentedHolderOu, null)));

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    verifyNoInteractions(padOrganisationRoleService);

    assertThat(roles).containsExactlyInAnyOrderElementsOf(teamMemberOrgRoleSet);

  }

  @Test
  public void getRolesInHolderTeam_noConsented_appHolderUsed() {

    when(teamManagementService.getTeamMemberViewForTeamAndPerson(appHolderTeam, person))
        .thenReturn(Optional.of(appMemberView));

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    verify(pwaConsentOrganisationRoleService, times(1)).getCurrentHoldersOrgRolesForMasterPwa(any());

    assertThat(roles).containsExactlyInAnyOrderElementsOf(appMemberOrgRoleSet);

  }

  @Test
  public void getHolderOrgGroups_consentedHolderExists() {

    when(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(any())).thenReturn(Set.of(
        new MasterPwaHolderDto(consentedHolderOu, null)));

    var organisationGroupSet = pwaHolderTeamService.getHolderOrgGroups(detail);

    verifyNoInteractions(padOrganisationRoleService);

    assertThat(organisationGroupSet).containsExactly(consentedHolderGroup);

  }

  @Test
  public void getHolderOrgGroups_noConsented_appHolderUsed() {

    var organisationGroupSet = pwaHolderTeamService.getHolderOrgGroups(detail);

    verify(pwaConsentOrganisationRoleService, times(1)).getCurrentHoldersOrgRolesForMasterPwa(any());

    assertThat(organisationGroupSet).containsExactly(appHolderGroup);

  }

  @Test
  public void getPeopleWithHolderTeamRole_hasRole() {

    when(teamService.getAllOrganisationTeams()).thenReturn(List.of(appHolderTeam));

    var people = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(people).containsOnly(person);

  }

  @Test
  public void getPeopleWithHolderTeamRole_doesntHaveRole() {

    when(teamService.getAllOrganisationTeams()).thenReturn(List.of(consentedHolderTeam));

    var people = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(people).isEmpty();

  }

}