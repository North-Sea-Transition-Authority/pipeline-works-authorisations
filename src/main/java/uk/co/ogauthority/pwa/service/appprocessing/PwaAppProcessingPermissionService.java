package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaAppProcessingPermissionService {

  private final TeamService teamService;

  @Autowired
  public PwaAppProcessingPermissionService(TeamService teamService) {
    this.teamService = teamService;
  }

  public Set<PwaAppProcessingPermission> getProcessingPermissions(WebUserAccount user) {

    Optional<PwaTeamMember> userRegTeamMembership = teamService
        .getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson());

    if (userRegTeamMembership.isEmpty()) {
      return Set.of();
    }

    Set<PwaRegulatorRole> roles = userRegTeamMembership.get().getRoleSet().stream()
        .map(pwaRole -> PwaRegulatorRole.getValueByPortalTeamRoleName(pwaRole.getName()))
        .collect(Collectors.toSet());

    return PwaAppProcessingPermission.stream()
        .filter(permission -> {

          switch (permission) {

            case ACCEPT_INITIAL_REVIEW:
              return roles.contains(PwaRegulatorRole.PWA_MANAGER);
            default:
              return false;

          }

        })
        .collect(Collectors.toSet());

  }

}
