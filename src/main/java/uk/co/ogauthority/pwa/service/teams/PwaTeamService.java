package uk.co.ogauthority.pwa.service.teams;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.UserTeamRolesView;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

/**
 * TeamService wrapper that answers common PWA team questions.
 */
@Service
public class PwaTeamService {

  private final TeamQueryService teamQueryService;
  private final UserAccountService userAccountService;

  @Autowired
  public PwaTeamService(TeamQueryService teamQueryService, UserAccountService userAccountService) {
    this.teamQueryService = teamQueryService;
    this.userAccountService = userAccountService;
  }

  public List<TeamMemberView> getMembersWithRegulatorRole(Role role) {
    return teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, role);
  }

  public Set<Person> getPeopleWithRegulatorRole(Role role) {
    Set<Integer> wuaIdSet = teamQueryService.getUsersOfStaticTeamWithRole(TeamType.REGULATOR, role).stream()
        .map(UserTeamRolesView::wuaId)
        .map(Long::intValue)
        .collect(Collectors.toSet());

    return userAccountService.getPersonsByWuaIdSet(wuaIdSet);
  }

  public List<TeamMemberView> getTeamMembersWithRegulatorRole(Role role) {
    return teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, role);
  }

  public Set<Person> getPeopleByConsulteeGroupAndRoleIn(ConsulteeGroup consulteeGroup, Set<Role> roles) {
    var teamType = TeamType.CONSULTEE;
    var teamScopeReference = TeamScopeReference.from(consulteeGroup.getId(), teamType);
    Set<Integer> wuaIdSet = teamQueryService.getUsersOfScopedTeam(teamType, teamScopeReference).stream()
        .filter(member -> member.roles().stream().anyMatch(roles::contains))
        .map(UserTeamRolesView::wuaId)
        .map(Long::intValue)
        .collect(Collectors.toSet());

    return userAccountService.getPersonsByWuaIdSet(wuaIdSet);
  }
}
