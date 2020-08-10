package uk.co.ogauthority.pwa.service.appprocessing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaAppProcessingPermissionService {

  private final TeamService teamService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;

  @Autowired
  public PwaAppProcessingPermissionService(TeamService teamService,
                                           ConsulteeGroupTeamService consulteeGroupTeamService) {
    this.teamService = teamService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
  }

  public Set<PwaAppProcessingPermission> getProcessingPermissions(WebUserAccount user) {

    Optional<PwaTeamMember> userRegTeamMembershipOpt = teamService
        .getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson());

    Set<PwaRegulatorRole> roles = userRegTeamMembershipOpt
        .map(regTeamMembership -> regTeamMembership.getRoleSet().stream()
            .map(pwaRole -> PwaRegulatorRole.getValueByPortalTeamRoleName(pwaRole.getName()))
            .collect(Collectors.toSet()))
        .orElse(Set.of());

    Set<ConsulteeGroupMemberRole> consulteeGroupRoles = new HashSet<>();
    consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())
        .forEach(member -> consulteeGroupRoles.addAll(member.getRoles()));

    return PwaAppProcessingPermission.stream()
        .filter(permission -> {

          switch (permission) {

            case ACCEPT_INITIAL_REVIEW:
              return roles.contains(PwaRegulatorRole.PWA_MANAGER);
            case CASE_OFFICER_REVIEW:
            case VIEW_CONSULTATIONS:
            case EDIT_CONSULTATIONS:
              return roles.contains(PwaRegulatorRole.CASE_OFFICER);
            case ASSIGN_RESPONDER:
              return consulteeGroupRoles.contains(ConsulteeGroupMemberRole.RECIPIENT)
                  || consulteeGroupRoles.contains(ConsulteeGroupMemberRole.RESPONDER);
            default:
              return false;

          }

        })
        .collect(Collectors.toSet());

  }

}
