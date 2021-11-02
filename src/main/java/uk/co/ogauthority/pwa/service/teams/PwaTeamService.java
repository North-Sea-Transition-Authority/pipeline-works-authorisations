package uk.co.ogauthority.pwa.service.teams;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;

/**
 * TeamService wrapper that answers common PWA team questions.
 */
@Service
public class PwaTeamService {

  private final TeamService teamService;

  @Autowired
  public PwaTeamService(TeamService teamService) {
    this.teamService = teamService;
  }

  public Set<Person> getPeopleWithRegulatorRole(PwaRegulatorRole pwaRegulatorRole) {
    return teamService.getTeamMembers(teamService.getRegulatorTeam()).stream()
        .filter(member -> member.getRoleSet().stream()
            .map(PwaRole::getName)
            .anyMatch(roleName -> roleName.equals(pwaRegulatorRole.getPortalTeamRoleName())))
        .map(PwaTeamMember::getPerson)
        .collect(Collectors.toSet());
  }

}
