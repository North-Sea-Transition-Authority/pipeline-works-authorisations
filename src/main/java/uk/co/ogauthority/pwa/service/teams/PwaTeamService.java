package uk.co.ogauthority.pwa.service.teams;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;

// TODO: Remove when we remove use of Person
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

  public Set<Person> getPeopleWithRegulatorRole(Role role) {
    return teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, role).stream()
        .map(this::getPersonIdFromWuaId)
        .collect(Collectors.toSet());
  }

  public List<TeamMemberView> getTeamMembersWithRegulatorRole(Role role) {
    return teamQueryService.getMembersOfStaticTeamWithRole(TeamType.REGULATOR, role);
  }

  @VisibleForTesting
  Person getPersonIdFromWuaId(TeamMemberView member) {
    var webUserAccount = userAccountService.getWebUserAccount(Math.toIntExact(member.wuaId()));
    return webUserAccount.getLinkedPerson();
  }
}
