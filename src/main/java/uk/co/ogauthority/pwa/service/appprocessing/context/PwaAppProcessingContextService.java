package uk.co.ogauthority.pwa.service.appprocessing.context;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.util.ApplicationContextUtils;

@Service
public class PwaAppProcessingContextService {

  private final PwaApplicationDetailService detailService;
  private final PwaAppProcessingPermissionService appProcessingPermissionService;
  private final ApplicationDetailSearcher applicationDetailSearcher;
  private final AppFileService appFileService;

  @Autowired
  public PwaAppProcessingContextService(PwaApplicationDetailService detailService,
                                        PwaAppProcessingPermissionService appProcessingPermissionService,
                                        ApplicationDetailSearcher applicationDetailSearcher,
                                        AppFileService appFileService) {
    this.detailService = detailService;
    this.appProcessingPermissionService = appProcessingPermissionService;
    this.applicationDetailSearcher = applicationDetailSearcher;
    this.appFileService = appFileService;
  }

  /**
   * Construct an application processing context to provide common objects associated with a PWA application and perform
   * standard permission checks.
   * @return application processing context if app is in right state and user has right permissions, throw relevant exceptions otherwise
   */
  public PwaAppProcessingContext validateAndCreate(PwaAppProcessingContextParams contextParams) {

    var applicationId = contextParams.getApplicationId();
    var context = getProcessingContext(applicationId, contextParams.getAuthenticatedUserAccount());

    ApplicationContextUtils.performAppStatusCheck(contextParams.getStatus(), context.getApplicationDetail());

    performPermissionCheck(
        contextParams.getAppProcessingPermissions(),
        context.getAppProcessingPermissions(),
        contextParams.getAuthenticatedUserAccount(),
        applicationId);

    if (contextParams.getFileId() != null) {
      getAndSetAppFile(context, contextParams.getFileId());
    }

    return context;

  }

  /**
   * Construct an application context to provide common objects associated with a PWA application.
   * @param applicationId for the PWA application
   * @param authenticatedUser trying to access the PWA application
   * @return application context object with app detail, users permissions etc populated
   */
  @VisibleForTesting
  PwaAppProcessingContext getProcessingContext(Integer applicationId,
                                                      AuthenticatedUserAccount authenticatedUser) {

    var detail = detailService.getLastSubmittedApplicationDetail(applicationId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Could not find last submitted version on applicationId:" + applicationId));
    var processingPermissions = appProcessingPermissionService.getProcessingPermissions(authenticatedUser);

    if (processingPermissions.isEmpty()) {
      throw new AccessDeniedException(
          String.format("User with WUA ID: %s has no app processing permissions", authenticatedUser.getWuaId()));
    }

    var caseSummaryView = applicationDetailSearcher.searchByApplicationDetailId(detail.getId())
        .map(CaseSummaryView::from)
        .orElse(null);

    return new PwaAppProcessingContext(detail, authenticatedUser, processingPermissions, caseSummaryView);

  }

  /**
   * If the user has ALL of the required permissions then pass, otherwise throw a relevant exception.
   */
  private void performPermissionCheck(Set<PwaAppProcessingPermission> requiredPermissions,
                                      Set<PwaAppProcessingPermission> usersPermissions,
                                      AuthenticatedUserAccount user,
                                      int applicationId) {

    if (!requiredPermissions.isEmpty()) {

      boolean userHasRequiredPermissions = usersPermissions.containsAll(requiredPermissions);

      if (!userHasRequiredPermissions) {
        throwPermissionException(user.getWuaId(), applicationId, requiredPermissions);
      }

    }

  }

  private void throwPermissionException(int wuaId, int applicationId,
                                        Set<PwaAppProcessingPermission> requiredPermissions) {
    throw new AccessDeniedException(
        String.format(
            "User with wua ID: %s can't access processing for PWA application with ID: %s as they do not have the required permissions: %s",
            wuaId,
            applicationId,
            requiredPermissions
        )
    );
  }

  /**
   * If a file is found for the requested ID (and it's on the same app as the context), then add to the context.
   * Otherwise throw a relevant exception.
   */
  private void getAndSetAppFile(PwaAppProcessingContext context, String fileId) {

    var appFile = appFileService.getAppFileByPwaApplicationAndFileId(context.getPwaApplication(), fileId);
    if (!Objects.equals(appFile.getPwaApplication(), context.getPwaApplication())) {
      throw new AccessDeniedException(
          String.format("AppFile app (%s) didn't match the app processing context's app (%s)",
              appFile.getPwaApplication().getId(),
              context.getPwaApplication().getId()));
    }
    context.setAppFile(appFile);

  }

}
