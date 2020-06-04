package uk.co.ogauthority.pwa.service.pwaapplications.context;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.util.ApplicationContextUtils;

@Service
public class PwaApplicationContextService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaApplicationContextService.class);

  private final PwaApplicationDetailService detailService;
  private final PwaContactService pwaContactService;
  private final PadPipelineService padPipelineService;
  private final PadFileService padFileService;

  @Autowired
  public PwaApplicationContextService(PwaApplicationDetailService detailService,
                                      PwaContactService pwaContactService,
                                      PadPipelineService padPipelineService,
                                      PadFileService padFileService) {
    this.detailService = detailService;
    this.pwaContactService = pwaContactService;
    this.padPipelineService = padPipelineService;
    this.padFileService = padFileService;
  }

  /**
   * Construct an application context to provide common objects associated with a PWA application and perform standard permission checks.
   * @return application context if app is in right state and user has right privileges, throw relevant exceptions otherwise
   */
  public PwaApplicationContext validateAndCreate(PwaApplicationContextParams contextParams) {

    var applicationId = contextParams.getApplicationId();
    var context = getApplicationContext(applicationId, contextParams.getAuthenticatedUserAccount());

    ApplicationContextUtils.performAppStatusCheck(contextParams.getStatus(), context.getApplicationDetail());
    performApplicationTypeCheck(contextParams.getTypes(), context.getApplicationType(), applicationId);
    performPrivilegeCheck(
        contextParams.getPermissions(),
        context.getUserRoles(),
        contextParams.getAuthenticatedUserAccount(),
        applicationId);

    if (contextParams.getPadPipelineId() != null) {
      getAndSetPipeline(context, contextParams.getPadPipelineId());
    }

    if (contextParams.getFileId() != null) {
      getAndSetPadFile(context, contextParams.getFileId());
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

  /**
   * If a file is found for the requested ID (and it's on the same app as the context), then add to the context.
   * Otherwise throw a relevant exception.
   */
  private void getAndSetPadFile(PwaApplicationContext context, String fileId) {

    try {
      var padFile = padFileService.getPadFileByPwaApplicationDetailAndFileId(context.getApplicationDetail(), fileId);
      if (!Objects.equals(padFile.getPwaApplicationDetail(), context.getApplicationDetail())) {
        throw new AccessDeniedException(
            String.format("PadFile app detail (%s) didn't match the app context's app detail (%s)",
                padFile.getPwaApplicationDetail().getId(),
                context.getApplicationDetail().getId()));
      }
      context.setPadFile(padFile);
    } catch (Exception e) {
      // Ignore this error for now
      // TODO: PWA-588 - Remove this try/catch block.
      LOGGER.error(e.toString());
    }
  }

}
