package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class PwaApplicationPermissionService {

  private final PwaContactService pwaContactService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final ApplicationInvolvementService applicationInvolvementService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PwaApplicationPermissionService(PwaContactService pwaContactService,
                                         PwaHolderTeamService pwaHolderTeamService,
                                         ApplicationInvolvementService applicationInvolvementService,
                                         TeamQueryService teamQueryService) {
    this.pwaContactService = pwaContactService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.applicationInvolvementService = applicationInvolvementService;
    this.teamQueryService = teamQueryService;
  }

  private UserRolesForApplicationDto getUserRolesForApplication(PwaApplicationDetail detail, WebUserAccount user) {

    var person = user.getLinkedPerson();
    var contactRoles = pwaContactService.getContactRoles(detail.getPwaApplication(), person);

    var holderTeamRoles = pwaHolderTeamService.getRolesInHolderTeam(detail, user);

    Set<Role> regulatorRoles = teamQueryService.getTeamRolesViewsByUserAndTeamType(user.getWuaId(), TeamType.REGULATOR).stream()
        .flatMap(userTeamRolesView -> userTeamRolesView.roles().stream())
        .collect(Collectors.toSet());

    var consulteeRoles = applicationInvolvementService
        .getConsultationInvolvement(detail.getPwaApplication(), user)
        .map(ConsultationInvolvementDto::getConsulteeRoles)
        .orElse(Set.of());

    return new UserRolesForApplicationDto(contactRoles, holderTeamRoles, regulatorRoles, consulteeRoles);

  }

  public Set<PwaApplicationPermission> getPermissions(PwaApplicationDetail detail, WebUserAccount user) {

    var userRolesForApplication = getUserRolesForApplication(detail, user);

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
