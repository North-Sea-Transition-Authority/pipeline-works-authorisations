package uk.co.ogauthority.pwa.service.teams;

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
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class PwaHolderTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PwaHolderService pwaHolderService;

  private PwaHolderTeamService pwaHolderTeamService;

  private PortalOrganisationGroup holderOrgGroup;
  private PortalOrganisationUnit holderOrgUnit;
  private PwaOrganisationTeam holderOrgTeam;

  private PwaApplicationDetail detail;

  private Person person;
  private WebUserAccount webUserAccount;


  @Before
  public void setUp() {

    pwaHolderTeamService = new PwaHolderTeamService(
        teamService,
        portalOrganisationsAccessor,
        pwaHolderService);

    person = PersonTestUtil.createDefaultPerson();
    webUserAccount = new WebUserAccount(1, person);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    holderOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "O", "O");
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
  public void isPersonInHolderTeam_holderExists_personInHolderTeam() {

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of(holderOrgTeam));

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail, person);

    assertThat(inTeam).isTrue();

  }

  @Test
  public void isPersonInHolderTeam_holderExists_personNotInHolderTeam() {

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of());

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeam(detail, person);

    assertThat(inTeam).isFalse();

  }

  @Test
  public void isPersonInHolderTeamWithRole_holderExists_personInHolderTeamWithRole() {

    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of(holderOrgTeam));

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isTrue();

  }

  @Test
  public void isPersonInHolderTeamWithRole_holderExists_personNotInHolderTeamWithRole() {

    when(teamService.getOrganisationTeamListIfPersonInRole(person, List.of(PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)))
        .thenReturn(List.of());

    boolean inTeam = pwaHolderTeamService.isPersonInHolderTeamWithRole(detail.getMasterPwa(), person, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER);

    assertThat(inTeam).isFalse();

  }

  @Test
  public void getRolesInHolderTeam_holderExists_personNotInHolderTeam() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).isEmpty();


  }

  @Test
  public void getRolesInHolderTeam_holderExists_personInHolderTeam() {

    var roles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    assertThat(roles).containsExactlyInAnyOrder(PwaOrganisationRole.values());


  }


  @Test
  public void getPeopleWithHolderTeamRole_singlePersonInHolderTeam() {


    var people = pwaHolderTeamService.getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_CREATOR);

    assertThat(people).containsOnly(person);

  }


  @Test
  public void getPersonsInHolderTeam_singlePersonInHolderTeam() {


    var people = pwaHolderTeamService.getPersonsInHolderTeam(detail);
    assertThat(people).containsOnly(person);

  }

  @Test
  public void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasRole(){

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of(holderOrgTeam));

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(orgUnits).contains(holderOrgUnit);

  }

  @Test
  public void getPortalOrganisationUnitsWhereUserHasAnyOrgRole_userHasNoRole(){

    when(teamService.getOrganisationTeamListIfPersonInRole(person, EnumSet.allOf(PwaOrganisationRole.class)))
        .thenReturn(List.of());

    var orgUnits = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
        webUserAccount, EnumSet.allOf(PwaOrganisationRole.class));

    assertThat(orgUnits).isEmpty();
  }


}