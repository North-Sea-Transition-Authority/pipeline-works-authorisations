package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Collections;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;

@Service
public class PwaApplicationContextService {

  private final PwaApplicationDetailService detailService;
  private final PwaContactService pwaContactService;

  @Autowired
  public PwaApplicationContextService(PwaApplicationDetailService detailService,
                                      PwaContactService pwaContactService) {
    this.detailService = detailService;
    this.pwaContactService = pwaContactService;
  }

  /**
   * Construct an application context to provide common objects associated with a PWA application.
   * @param applicationId for the PWA application
   * @param authenticatedUser trying to access the PWA application
   * @param requiredPermissions user must have to be able to access the PWA application
   * @param appStatus that the PWA application must be in
   * @param applicationTypes the application must be one of
   * @return application context if app is in right state and user has right privileges, throw relevant exceptions otherwise
   */
  public PwaApplicationContext getApplicationContext(Integer applicationId,
                                                     AuthenticatedUserAccount authenticatedUser,
                                                     Set<PwaApplicationPermission> requiredPermissions,
                                                     PwaApplicationStatus appStatus,
                                                     Set<PwaApplicationType> applicationTypes) {

    var detail = appStatus != null
        ? detailService.getTipDetailWithStatus(applicationId, appStatus)
        : detailService.getTipDetail(applicationId);

    var application = detail.getPwaApplication();

    performApplicationTypeCheck(applicationTypes, application.getApplicationType(), applicationId);

    var roles = pwaContactService.getContactRoles(detail.getPwaApplication(), authenticatedUser.getLinkedPerson());

    performPrivilegeCheck(requiredPermissions, roles, authenticatedUser, applicationId);

    return new PwaApplicationContext(detail, authenticatedUser, roles);

  }

  private void performApplicationTypeCheck(Set<PwaApplicationType> applicationTypes,
                                           PwaApplicationType applicationType,
                                           int applicationId) {
    if (!applicationTypes.isEmpty() && !applicationTypes.contains(applicationType)) {
      throw new AccessDeniedException(
          String.format("PWA application with ID: %s and type: %s cannot access route defined for app types: %s",
              applicationId,
              applicationType,
              applicationTypes
          )
      );
    }
  }

  private void performPrivilegeCheck(Set<PwaApplicationPermission> requiredPermissions,
                                     Set<PwaContactRole> usersRoles,
                                     AuthenticatedUserAccount user,
                                     int applicationId) {

    if (usersRoles.isEmpty()) {
      throwPermissionException(user.getWuaId(), applicationId, requiredPermissions);
    }

    if (!requiredPermissions.isEmpty()) {

      boolean userHasRequiredPermissions = requiredPermissions.stream()
          .noneMatch(p -> Collections.disjoint(p.getRoles(), usersRoles));

      if (!userHasRequiredPermissions) {
        throwPermissionException(user.getWuaId(), applicationId, requiredPermissions);
      }
    }

  }

  private void throwPermissionException(int wuaId, int applicationId, Set<PwaApplicationPermission> requiredPermissions) {
    throw new AccessDeniedException(
        String.format(
            "User with wua ID: %s cannot access PWA application with ID: %s as they do not have the required permissions: %s",
            wuaId,
            applicationId,
            requiredPermissions
        )
    );
  }

}
