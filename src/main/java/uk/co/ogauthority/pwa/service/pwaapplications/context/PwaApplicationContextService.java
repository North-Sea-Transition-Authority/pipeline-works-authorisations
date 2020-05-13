package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;

@Service
public class PwaApplicationContextService {

  private final PwaApplicationDetailService detailService;
  private final PwaContactService pwaContactService;
  private final PadPipelineService padPipelineService;

  @Autowired
  public PwaApplicationContextService(PwaApplicationDetailService detailService,
                                      PwaContactService pwaContactService,
                                      PadPipelineService padPipelineService) {
    this.detailService = detailService;
    this.pwaContactService = pwaContactService;
    this.padPipelineService = padPipelineService;
  }

  /**
   * Construct an application context to provide common objects associated with a PWA application and perform standard permission checks.
   * @return application context if app is in right state and user has right privileges, throw relevant exceptions otherwise
   */
  public PwaApplicationContext validateAndCreate(PwaApplicationContextParams contextParams) {

    var applicationId = contextParams.getApplicationId();
    var context = getApplicationContext(applicationId, contextParams.getAuthenticatedUserAccount());

    performAppStatusCheck(contextParams.getStatus(), context.getApplicationDetail());
    performApplicationTypeCheck(contextParams.getTypes(), context.getApplicationType(), applicationId);
    performPrivilegeCheck(
        contextParams.getPermissions(),
        context.getUserRoles(),
        contextParams.getAuthenticatedUserAccount(),
        applicationId);

    if (contextParams.getPadPipelineId() != null) {
      getAndSetPipeline(context, contextParams.getPadPipelineId());
    }

    return context;

  }

  /**
   * Construct an application context to provide common objects associated with a PWA application.
   * @param applicationId for the PWA application
   * @param authenticatedUser trying to access the PWA application
   * @return application context object with app detail, users roles etc populated
   */
  public PwaApplicationContext getApplicationContext(Integer applicationId,
                                                     AuthenticatedUserAccount authenticatedUser) {
    var detail = detailService.getTipDetail(applicationId);
    var roles = pwaContactService.getContactRoles(detail.getPwaApplication(), authenticatedUser.getLinkedPerson());
    return new PwaApplicationContext(detail, authenticatedUser, roles);
  }


  /**
   * If the application status matches the required one, pass, otherwise throw relevant exception.
   */
  private void performAppStatusCheck(PwaApplicationStatus expectedStatus,
                                     PwaApplicationDetail pwaApplicationDetail) {

    if (expectedStatus != null && !expectedStatus.equals(pwaApplicationDetail.getStatus())) {
      throw new PwaEntityNotFoundException(
          String.format("PwaApplicationDetailId:%s Did not have expected status:%s. Actual status:%s",
              pwaApplicationDetail.getId(),
              expectedStatus,
              pwaApplicationDetail.getStatus()
          )
      );
    }

  }

  /**
   * If the application type matches the required one, pass, otherwise throw relevant exception.
   */
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

  /**
   * If the user has ALL of the required permissions then pass, otherwise throw a relevant exception.
   */
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

  private void throwPermissionException(int wuaId, int applicationId,
                                        Set<PwaApplicationPermission> requiredPermissions) {
    throw new AccessDeniedException(
        String.format(
            "User with wua ID: %s cannot access PWA application with ID: %s as they do not have the required permissions: %s",
            wuaId,
            applicationId,
            requiredPermissions
        )
    );
  }

  /**
   * If a pipeline is found for the requested ID (and it's on the same app as the context), then add to the context.
   * Otherwise throw a relevant exception.
   */
  private void getAndSetPipeline(PwaApplicationContext context, int padPipelineId) {

    var pipeline = padPipelineService.getById(padPipelineId);

    if (!Objects.equals(pipeline.getPwaApplicationDetail(), context.getApplicationDetail())) {
      throw new AccessDeniedException(String.format("PadPipeline app detail (%s) didn't match the app context's app detail (%s)",
          pipeline.getPwaApplicationDetail().getId(),
          context.getApplicationDetail().getId()));
    }

    context.setPadPipeline(pipeline);

  }

}
