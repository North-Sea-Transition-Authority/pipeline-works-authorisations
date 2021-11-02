package uk.co.ogauthority.pwa.service.teams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees.ConsulteeGroupTeamManagementController;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.enums.teams.ManageTeamType;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationTeam;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorTeam;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class ManageTeamServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private PortalOrganisationGroup orgGroup;
  private PwaOrganisationTeam orgGroupTeam;

  private ManageTeamService manageTeamService;

  private WebUserAccount user;

  private Map.Entry<ManageTeamType, String> ogaTeamEntry;
  private Map.Entry<ManageTeamType, String> orgTeamEntry;
  private Map.Entry<ManageTeamType, String> consulteeTeamEntry;

  @Before
  public void setUp() {

    orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "TEST GROUP", "TG");
    orgGroupTeam = new PwaOrganisationTeam(2, "test team", "a test team", orgGroup);

    when(teamService.getRegulatorTeam()).thenReturn(new PwaRegulatorTeam(1, "name", "desc"));

    manageTeamService = new ManageTeamService(teamService, consulteeGroupTeamService);

    user = new WebUserAccount(1, new Person(1, "forename", "surname", null, null));

    ogaTeamEntry = entry(ManageTeamType.REGULATOR_TEAM, ReverseRouter.route(on(PortalTeamManagementController.class)
        .renderTeamMembers(teamService.getRegulatorTeam().getId(), null)));

    orgTeamEntry = entry(ManageTeamType.ORGANISATION_TEAMS,
        ReverseRouter.route(on(PortalTeamManagementController.class).renderManageableTeams(null)));

    consulteeTeamEntry = entry(ManageTeamType.CONSULTEE_GROUP_TEAMS,
        ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)));

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_regulatorAdmin() {

    returnMemberWithRegRoles(Set.of(PwaRegulatorRole.TEAM_ADMINISTRATOR));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(
        ogaTeamEntry,
        consulteeTeamEntry
    );

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_regulatorOrgManager() {

    returnMemberWithRegRoles(Set.of(PwaRegulatorRole.ORGANISATION_MANAGER));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(orgTeamEntry);

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_regulatorAdmin_and_orgManager() {

    returnMemberWithRegRoles(Set.of(PwaRegulatorRole.ORGANISATION_MANAGER, PwaRegulatorRole.TEAM_ADMINISTRATOR));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(
        ogaTeamEntry,
        orgTeamEntry,
        consulteeTeamEntry
    );

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_consulteeAccessManager() {

    var consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "n");
    when(consulteeGroupTeamService.getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER)).thenReturn(Set.of(consulteeGroupDetail.getConsulteeGroup()));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(consulteeTeamEntry);

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_consulteeAccessManager_and_orgManager() {

    var consulteeGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "n");
    when(consulteeGroupTeamService.getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER)).thenReturn(Set.of(consulteeGroupDetail.getConsulteeGroup()));

    returnMemberWithRegRoles(Set.of(PwaRegulatorRole.ORGANISATION_MANAGER));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(
        orgTeamEntry,
        consulteeTeamEntry
    );

  }

  @Test
  public void getManageTeamTypesAndUrlsForUser_orgGroupTeamManagerOnly() {


    when(teamService.getOrganisationTeamListIfPersonInRole(user.getLinkedPerson(), EnumSet.of(PwaOrganisationRole.TEAM_ADMINISTRATOR)))
        .thenReturn(List.of(orgGroupTeam));

    assertThat(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).containsExactly(
        orgTeamEntry
    );

  }

  private void returnMemberWithRegRoles(Set<PwaRegulatorRole> regulatorRoles) {

    var teamAdminMember = TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(), user.getLinkedPerson(), regulatorRoles);

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(
        Optional.of(teamAdminMember));

  }


}
