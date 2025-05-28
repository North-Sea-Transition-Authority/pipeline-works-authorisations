package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
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
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaHolderTeamServiceTest {

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

  private PwaApplicationDetail detail;

  private Person person;
  private WebUserAccount webUserAccount;
  private PortalOrganisationGroup holderOrgGroup;
  private PwaTeamMember personHolderTeamMembership;
  private int orgGrpId;
  private EnumSet<Role> orgRoles;

  @BeforeEach
  void setUp() {

    person = PersonTestUtil.createDefaultPerson();
    webUserAccount = new WebUserAccount(1, person);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    orgGrpId = 1;
    holderOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(orgGrpId, "O", "O");

    holderOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "OO", holderOrgGroup);

    orgRoles = EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles());
  }

  @Test
  void isPersonInHolderTeam_holderExists_personInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(orgRoles)))
        .thenReturn(true);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeam_holderExists_personNotInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(orgRoles)))
        .thenReturn(false);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isFalse();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personInHolderTeamWithRole() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(TeamType.ORGANISATION), any(TeamScopeReference.class),
        eq(Set.of(Role.AS_BUILT_NOTIFICATION_SUBMITTER)))).thenReturn(true);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), webUserAccount, Role.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personNotInHolderTeamWithRole() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(TeamType.ORGANISATION), any(TeamScopeReference.class),
        eq(Set.of(Role.AS_BUILT_NOTIFICATION_SUBMITTER)))).thenReturn(false);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), webUserAccount, Role.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isFalse();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personNotInHolderTeam() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getRolesForUserInScopedTeams(webUserAccount.getWuaId(), TeamType.ORGANISATION, Set.of(String.valueOf(1))))
        .thenReturn(Set.of());

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, webUserAccount);

    assertThat(roles).isEmpty();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personInHolderTeam() {
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getRolesForUserInScopedTeams(webUserAccount.getWuaId(), TeamType.ORGANISATION, Set.of(String.valueOf(1))
    ))
        .thenReturn(Set.of(Role.TEAM_ADMINISTRATOR, Role.APPLICATION_CREATOR));

    var result = pwaHolderTeamService.getRolesInHolderTeam(detail, webUserAccount);

    assertThat(result).containsOnly(Role.TEAM_ADMINISTRATOR, Role.APPLICATION_CREATOR);
  }


  @Test
  void getPeopleWithHolderTeamRole_singlePersonInHolderTeam() {
    var role = Role.APPLICATION_CREATOR;
    var userTeamRolesView = new UserTeamRolesView(1L, null, null, List.of(role, Role.APPLICATION_SUBMITTER));
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getUsersOfScopedTeams(TeamType.ORGANISATION, Set.of(String.valueOf(orgGrpId)))).thenReturn(List.of(userTeamRolesView));
    when(userAccountService.getPersonsByWuaIdSet(Set.of(1))).thenReturn(Set.of(person));

    var result = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, role);

    assertThat(result).containsOnly(person);
  }

  @Test
  void getPeopleWithHolderTeamRole_PersonDoesntHaveTheRole() {
    var userTeamRolesView = new UserTeamRolesView(1L, null, null, List.of(Role.APPLICATION_CREATOR, Role.APPLICATION_SUBMITTER));
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getUsersOfScopedTeams(TeamType.ORGANISATION, Set.of(String.valueOf(orgGrpId)))).thenReturn(List.of(userTeamRolesView));

    var result = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, Role.TEAM_ADMINISTRATOR);

    assertThat(result).isEmpty();
  }

  @Test
  void getPersonsInHolderTeam_singlePersonInHolderTeam() {
    var userTeamRolesView = new UserTeamRolesView(1L, null, null, List.of());
    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));
    when(teamQueryService.getUsersOfScopedTeam(eq(TeamType.ORGANISATION), any(TeamScopeReference.class)))
        .thenReturn(List.of(userTeamRolesView));
    when(userAccountService.getPersonsByWuaIdSet(Set.of(1))).thenReturn(Set.of(person));

    var people = pwaHolderTeamService.getPersonsInHolderTeam(detail);

    assertThat(people).containsOnly(person);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasRole(){
    int teamId = 1;

    var team = new Team();
    team.setScopeId(String.valueOf(teamId));

    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(List.of(holderOrgGroup)))
        .thenReturn(List.of(holderOrgUnit));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, orgRoles))
        .thenReturn(List.of(team));
    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(holderOrgGroup));

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(webUserAccount, orgRoles);

    assertThat(orgUnits).contains(holderOrgUnit);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasNoRole(){
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, orgRoles))
        .thenReturn(List.of());

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of())).thenReturn(List.of());

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, orgRoles);

    assertThat(orgUnits).isEmpty();
    verify(portalOrganisationsAccessor).getOrganisationUnitsForOrganisationGroupsIn(List.of());
  }

  @Test
  void getPortalOrganisationGroupsWhereUserHasRoleIn_userHasRole(){
    int teamId = 1;

    var team = new Team();
    team.setScopeId(String.valueOf(teamId));
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, orgRoles))
        .thenReturn(List.of(team));

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(teamId))).thenReturn(List.of(holderOrgGroup));

    var result = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        webUserAccount, orgRoles);

    assertThat(result).contains(holderOrgGroup);
  }

  @Test
  void getPortalOrganisationGroupsWhereUserHasRoleIn_userHasNoRole(){
    when(teamQueryService.getTeamsOfTypeUserHasAnyRoleIn(webUserAccount.getWuaId(), TeamType.ORGANISATION, orgRoles))
        .thenReturn(List.of());

    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of())).thenReturn(List.of());

    var result = pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasRoleIn(
        webUserAccount, orgRoles);

    assertThat(result).contains();
  }
}