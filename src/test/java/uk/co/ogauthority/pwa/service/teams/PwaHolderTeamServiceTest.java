package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@ExtendWith(MockitoExtension.class)
class PwaHolderTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PwaHolderService pwaHolderService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private UserAccountService userAccountService;

  @InjectMocks
  private PwaHolderTeamService pwaHolderTeamService;

  private PortalOrganisationUnit holderOrgUnit;
  private PwaOrganisationTeam holderOrgTeam;

  private PwaApplicationDetail detail;

  private Person person;
  private WebUserAccount webUserAccount;
  private PortalOrganisationGroup holderOrgGroup;
  private PwaTeamMember personHolderTeamMembership;

  @BeforeEach
  void setUp() {

    person = PersonTestUtil.createDefaultPerson();
    webUserAccount = new WebUserAccount(1, person);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    holderOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "O", "O");
    holderOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "OO", holderOrgGroup);
    holderOrgTeam = TeamTestingUtils.getOrganisationTeam(holderOrgGroup);

    personHolderTeamMembership = TeamTestingUtils.createOrganisationTeamMember(
        holderOrgTeam, person, EnumSet.allOf(PwaOrganisationRole.class));
  }

  @Test
  void isPersonInHolderTeam_holderExists_personInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(EnumSet.allOf(Role.class))))
        .thenReturn(true);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeam_holderExists_personNotInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(EnumSet.allOf(Role.class))))
        .thenReturn(false);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isFalse();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personInHolderTeamWithRole() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of(holderOrgTeam));

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personNotInHolderTeamWithRole() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of());

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isFalse();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personNotInHolderTeam() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).isEmpty();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personInHolderTeam() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(holderOrgTeam));
    when(teamService.getMembershipOfPersonInTeam(holderOrgTeam, person)).thenReturn(Optional.of(personHolderTeamMembership));

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).containsExactlyInAnyOrder(PwaOrganisationRole.values());
  }


  @Test
  void getPeopleWithHolderTeamRole_singlePersonInHolderTeam() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamService.getOrganisationTeamsForOrganisationGroups(Set.of(holderOrgGroup))).thenReturn(List.of(holderOrgTeam));
    when(teamService.getTeamMembers(holderOrgTeam)).thenReturn(List.of(personHolderTeamMembership));
    var people = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(people).containsOnly(person);
  }

  @Test
  void getPersonsInHolderTeam_singlePersonInHolderTeam() {
    var teamMemberView = mock(TeamMemberView.class);
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getMembersOfScopedTeam(eq(TeamType.ORGANISATION), any(TeamScopeReference.class))).thenReturn(List.of(teamMemberView));
    when(userAccountService.getWebUserAccount(anyInt())).thenReturn(webUserAccount);

    var people = pwaHolderTeamService.getPersonsInHolderTeam(detail);

    assertThat(people).containsOnly(person);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasRole(){
    var allowedRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());
    int teamId = 1;

    var team = new Team();
    team.setScopeId(String.valueOf(teamId));

    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(List.of(holderOrgGroup)))
        .thenReturn(List.of(holderOrgUnit));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, allowedRoles))
        .thenReturn(List.of(team));
    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(holderOrgGroup));

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(webUserAccount, allowedRoles);

    assertThat(orgUnits).contains(holderOrgUnit);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasNoRole(){
    var allowedRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, allowedRoles))
        .thenReturn(List.of());

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of())).thenReturn(List.of());

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles()));

    assertThat(orgUnits).isEmpty();
    verify(portalOrganisationsAccessor).getOrganisationUnitsForOrganisationGroupsIn(List.of());
  }

  @Test
  void getPortalOrganisationGroupsWhereUserHasRoleIn_userHasRole(){
    var allowedRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());
    int teamId = 1;

    var team = new Team();
    team.setScopeId(String.valueOf(teamId));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, allowedRoles))
        .thenReturn(List.of(team));

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(holderOrgGroup));

    var result = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        webUserAccount, EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles()));

    assertThat(result).contains(holderOrgGroup);
  }

  @Test
  void getPortalOrganisationGroupsWhereUserHasRoleIn_userHasNoRole(){
    var allowedRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, allowedRoles))
        .thenReturn(List.of());

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of())).thenReturn(List.of());

    var result = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        webUserAccount, EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles()));

    assertThat(result).contains();
  }
}