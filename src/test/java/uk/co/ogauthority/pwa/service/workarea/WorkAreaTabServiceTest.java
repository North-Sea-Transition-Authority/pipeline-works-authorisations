package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.testutils.TeamTestingUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaTabServiceTest {

  @Mock
  private TeamService teamService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private WorkAreaTabService workAreaTabService;

  private Person person = new Person(1, null, null, null, null);

  @Before
  public void setUp() {

    var regTeamMember = TeamTestingUtils.createRegulatorTeamMember(teamService.getRegulatorTeam(),
        person,
        Set.of(PwaRegulatorRole.PWA_MANAGER));

    var consulteeTeamMember = new ConsulteeGroupTeamMember(new ConsulteeGroup(), person, Set.of(
        ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER));

    // default everything returned
    var orgTeam = TeamTestingUtils.getOrganisationTeam(TeamTestingUtils.generateOrganisationGroup(1, "Test", "TEST"));
    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of(orgTeam));
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.of(regTeamMember));
    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of(consulteeTeamMember));

    workAreaTabService = new WorkAreaTabService(teamService, consulteeGroupTeamService);


  }

  @Test
  public void getDefaultTabForPerson_allTabsAvailable() {

    var defaultTabOpt = workAreaTabService.getDefaultTabForPerson(person);

    assertThat(defaultTabOpt).isPresent();

    assertThat(defaultTabOpt.get()).isEqualTo(WorkAreaTab.OPEN_APPLICATIONS);

  }

  @Test
  public void getDefaultTabForPerson_noTabs() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.empty());
    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of());

    var defaultTabOpt = workAreaTabService.getDefaultTabForPerson(person);

    assertThat(defaultTabOpt).isEmpty();

  }

  @Test
  public void getTabsAvailableToPerson_allTabs() {

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_APPLICATIONS, WorkAreaTab.OPEN_CONSULTATIONS);

  }

  @Test
  public void getTabsAvailableToPerson_industryOnly() {

    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.empty());
    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of());

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_APPLICATIONS);

  }

  @Test
  public void getTabsAvailableToPerson_regulatorOnly() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());
    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of());

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_APPLICATIONS);

  }

  @Test
  public void getTabsAvailableToPerson_consulteeOnly_recipientResponder() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.empty());

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);

  }

  @Test
  public void getTabsAvailableToPerson_consulteeOnly_accessManagerOnly() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.empty());

    var accessManagerTeamMember = new ConsulteeGroupTeamMember(new ConsulteeGroup(), person, Set.of(
        ConsulteeGroupMemberRole.ACCESS_MANAGER));

    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of(accessManagerTeamMember));

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).isEmpty();

  }

  @Test
  public void getTabsAvailableToPerson_noTabs() {

    when(teamService.getOrganisationTeamsPersonIsMemberOf(person)).thenReturn(List.of());
    when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)).thenReturn(Optional.empty());
    when(consulteeGroupTeamService.getTeamMembersByPerson(person)).thenReturn(List.of());

    var tabs = workAreaTabService.getTabsAvailableToPerson(person);

    assertThat(tabs).isEmpty();

  }

}