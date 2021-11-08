package uk.co.ogauthority.pwa.service.teams;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.teams.PortalTeamManagementController;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.enums.teams.ManageTeamType;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;

@Service
public class ManageTeamService {

  private final TeamService teamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public ManageTeamService(TeamService teamService,
                           ConsulteeGroupTeamService consulteeGroupTeamService) {

    this.teamService = teamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  public Map<ManageTeamType, String> getManageTeamTypesAndUrlsForUser(WebUserAccount user) {

    var teamTypeUrls = new EnumMap<ManageTeamType, String>(ManageTeamType.class);

    Set<PwaRegulatorRole> userRegRoles = teamService
        .getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())
        .map(member -> member.getRoleSet().stream()
            .map(role -> PwaRegulatorRole.getValueByPortalTeamRoleName(role.getName()))
            .collect(Collectors.toSet()))
        .orElse(Set.of());


    var userCanManageOrgTeams = !teamService.getOrganisationTeamListIfPersonInRole(
        user.getLinkedPerson(),
        EnumSet.of(PwaOrganisationRole.TEAM_ADMINISTRATOR)
    ).isEmpty();

    // if user in regulator team org manager role, can access org teams, or use is team admin for at least one org
    if (userRegRoles.contains(PwaRegulatorRole.ORGANISATION_MANAGER) || userCanManageOrgTeams) {
      teamTypeUrls.put(ManageTeamType.ORGANISATION_TEAMS, ManageTeamType.ORGANISATION_TEAMS.getLinkUrl());
    }

    // if user is a regulator team admin, can access OGA and consultee teams
    if (userRegRoles.contains(PwaRegulatorRole.TEAM_ADMINISTRATOR)) {

      teamTypeUrls.put(ManageTeamType.REGULATOR_TEAM,
          ReverseRouter.route(on(PortalTeamManagementController.class)
              .renderTeamMembers(teamService.getRegulatorTeam().getId(), null)));

      teamTypeUrls.put(ManageTeamType.CONSULTEE_GROUP_TEAMS, ManageTeamType.CONSULTEE_GROUP_TEAMS.getLinkUrl());

      return sortedMap(teamTypeUrls);

    }

    // if the user is an access manager for at least one consultee group, they can access the consultee team list
    var groupsUserIsAccessManagerFor = consulteeGroupTeamService.getGroupsUserHasRoleFor(user, ConsulteeGroupMemberRole.ACCESS_MANAGER);

    if (!groupsUserIsAccessManagerFor.isEmpty()) {
      teamTypeUrls.put(ManageTeamType.CONSULTEE_GROUP_TEAMS, ManageTeamType.CONSULTEE_GROUP_TEAMS.getLinkUrl());
    }

    return sortedMap(teamTypeUrls);

  }

  private Map<ManageTeamType, String> sortedMap(Map<ManageTeamType, String> map) {
    return map.entrySet().stream()
        .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayOrder()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

}
