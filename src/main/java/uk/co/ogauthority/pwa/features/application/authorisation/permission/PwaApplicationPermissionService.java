package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Service
public class PwaApplicationPermissionService {

  private final PwaContactService pwaContactService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final TeamService teamService;
  private final ApplicationInvolvementService applicationInvolvementService;

  @Autowired
  public PwaApplicationPermissionService(PwaContactService pwaContactService,
                                         PwaHolderTeamService pwaHolderTeamService,
                                         TeamService teamService,
                                         ApplicationInvolvementService applicationInvolvementService) {
    this.pwaContactService = pwaContactService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.teamService = teamService;
    this.applicationInvolvementService = applicationInvolvementService;
  }

  private UserRolesForApplicationDto getUserRolesForApplication(PwaApplicationDetail detail, Person person) {

    var contactRoles = pwaContactService.getContactRoles(detail.getPwaApplication(), person);

    var holderTeamRoles = pwaHolderTeamService.getRolesInHolderTeam(detail, person);

    var regulatorRoles = teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), person)
        .map(member -> member.getRoleSet().stream()
            .map(r -> PwaRegulatorRole.getValueByPortalTeamRoleName(r.getName()))
            .collect(Collectors.toSet()))
        .orElse(Set.of());

    var consulteeRoles = applicationInvolvementService
        .getConsultationInvolvement(detail.getPwaApplication(), person)
        .map(ConsultationInvolvementDto::getConsulteeRoles)
        .orElse(Set.of());

    return new UserRolesForApplicationDto(contactRoles, holderTeamRoles, regulatorRoles, consulteeRoles);

  }

  public Set<PwaApplicationPermission> getPermissions(PwaApplicationDetail detail, Person person) {

    var userRolesForApplication = getUserRolesForApplication(detail, person);

    return PwaApplicationPermission.stream()
        .filter(permission -> userHasPermission(permission, userRolesForApplication))
        .collect(Collectors.toSet());

  }

  private boolean userHasPermission(PwaApplicationPermission permission,
                                    UserRolesForApplicationDto userRolesForApplication) {

    boolean userHasContactRoles = userHasOneOrMoreRequiredPermissions(
        permission.getContactRoles(), userRolesForApplication.getUserContactRoles());

    boolean userHasHolderTeamRoles = userHasOneOrMoreRequiredPermissions(
        permission.getHolderTeamRoles(), userRolesForApplication.getUserHolderTeamRoles());

    boolean userHasRegulatorRoles = userHasOneOrMoreRequiredPermissions(
        permission.getRegulatorRoles(), userRolesForApplication.getUserRegulatorRoles());

    boolean userHasConsulteeRoles = userHasOneOrMoreRequiredPermissions(
        permission.getConsulteeRoles(), userRolesForApplication.getUserConsulteeRoles());

    return permission.getPermissionOverrideFunctionResult(userRolesForApplication)
        || (userHasContactRoles || userHasHolderTeamRoles || userHasRegulatorRoles || userHasConsulteeRoles);
  }

  private <T> boolean userHasOneOrMoreRequiredPermissions(Set<T> userPermissions,
                                                          Set<T> requiredPermissions) {
    return !Collections.disjoint(userPermissions, requiredPermissions);
  }

}
