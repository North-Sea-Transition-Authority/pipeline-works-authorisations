package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
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
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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

  @BeforeEach
  void setUp() {

    person = PersonTestUtil.createDefaultPerson();
    webUserAccount = new WebUserAccount(1, person);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var holderOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "O", "O");
    holderOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "OO", holderOrgGroup);

    when(portalOrganisationsAccessor.getOrganisationUnitsForOrganisationGroupsIn(List.of(holderOrgGroup)))
        .thenReturn(List.of(holderOrgUnit));

    when(pwaHolderService.getPwaHolderOrgGroups(detail.getMasterPwa())).thenReturn(Set.of(holderOrgGroup));

    holderOrgTeam = TeamTestingUtils.getOrganisationTeam(holderOrgGroup);
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(holderOrgTeam));
    when(teamService.getOrganisationTeamsForOrganisationGroups(Set.of(holderOrgGroup))).thenReturn(List.of(holderOrgTeam));

    var personHolderTeamMembership = TeamTestingUtils.createOrganisationTeamMember(
        holderOrgTeam, person, EnumSet.allOf(PwaOrganisationRole.class));
    when(teamService.getMembershipOfPersonInTeam(holderOrgTeam, person)).thenReturn(Optional.of(personHolderTeamMembership));
    when(teamService.getTeamMembers(holderOrgTeam)).thenReturn(List.of(personHolderTeamMembership));
  }

  @Test
  void isPersonInHolderTeam_holderExists_personInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(EnumSet.allOf(Role.class))))
        .thenReturn(true);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeam_holderExists_personNotInHolderTeam() {
    var teamType = TeamType.ORGANISATION;
    when(teamQueryService.userHasAtLeastOneScopedRole(eq((long) webUserAccount.getWuaId()), eq(teamType), any(TeamScopeReference.class), eq(EnumSet.allOf(Role.class))))
        .thenReturn(false);

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail.getMasterPwa(), webUserAccount);

    assertThat(inTeam).isFalse();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personInHolderTeamWithRole() {
    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of(holderOrgTeam));

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isTrue();
  }

  @Test
  void isPersonInHolderTeamWithRole_holderExists_personNotInHolderTeamWithRole() {
    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of());

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isFalse();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personNotInHolderTeam() {
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).isEmpty();
  }

  @Test
  void getRolesInHolderTeam_holderExists_personInHolderTeam() {

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).containsExactlyInAnyOrder(PwaOrganisationRole.values());
  }


  @Test
  void getPeopleWithHolderTeamRole_singlePersonInHolderTeam() {
    var people = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(people).containsOnly(person);
  }

  @Test
  void getPersonsInHolderTeam_singlePersonInHolderTeam() {
    var teamMemberView = mock(TeamMemberView.class);
    when(teamQueryService.getMembersOfScopedTeam(eq(TeamType.ORGANISATION), any(TeamScopeReference.class))).thenReturn(List.of(teamMemberView));
    when(userAccountService.getWebUserAccount(anyInt())).thenReturn(webUserAccount);

    var people = pwaHolderTeamService.getPersonsInHolderTeam(detail);

    assertThat(people).containsOnly(person);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasRole(){
    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of(holderOrgTeam));

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(orgUnits).contains(holderOrgUnit);
  }

  @Test
  void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasNoRole(){

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of());

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(orgUnits).isEmpty();
  }
}