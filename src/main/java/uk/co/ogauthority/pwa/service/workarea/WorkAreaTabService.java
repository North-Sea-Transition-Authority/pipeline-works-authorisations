package uk.co.ogauthority.pwa.service.workarea;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class WorkAreaTabService {

  private final TeamService teamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public WorkAreaTabService(TeamService teamService,
                            ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.teamService = teamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  public Optional<WorkAreaTab> getDefaultTabForPerson(Person person) {

    var availableTabs = getTabsAvailableToPerson(person);

    if (availableTabs.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(availableTabs.get(0));

  }

  public List<WorkAreaTab> getTabsAvailableToPerson(Person person) {

    var tabs = new ArrayList<WorkAreaTab>();

    var orgTeams = teamService.getOrganisationTeamsPersonIsMemberOf(person);
    boolean isRegulatorUser = teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person).isPresent();

    if (!orgTeams.isEmpty() || isRegulatorUser) {
      tabs.add(WorkAreaTab.OPEN_APPLICATIONS);
    }

    boolean consulteeUser = consulteeGroupTeamService.getTeamMembersByPerson(person).stream()
        .flatMap(groupTeamMember -> groupTeamMember.getRoles().stream())
        .anyMatch(role -> role == ConsulteeGroupMemberRole.RECIPIENT || role == ConsulteeGroupMemberRole.RESPONDER);

    if (consulteeUser) {
      tabs.add(WorkAreaTab.OPEN_CONSULTATIONS);
    }

    tabs.sort(Comparator.comparing(WorkAreaTab::getDisplayOrder));

    return tabs;

  }

}
