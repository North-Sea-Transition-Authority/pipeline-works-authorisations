package uk.co.ogauthority.pwa.features.application.authorisation.context;

import com.google.common.base.Stopwatch;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;

@Service
public class PwaApplicationContextService {

  private final PwaApplicationDetailService detailService;
  private final PadPipelineService padPipelineService;
  private final PadFileService padFileService;
  private final PwaApplicationPermissionService pwaApplicationPermissionService;
  private final MetricsProvider metricsProvider;

  private static final Logger LOGGER = LoggerFactory.getLogger(PwaApplicationContextService.class);

  @Autowired
  public PwaApplicationContextService(PwaApplicationDetailService detailService,
                                      PadPipelineService padPipelineService,
                                      PadFileService padFileService,
                                      PwaApplicationPermissionService pwaApplicationPermissionService,
                                      MetricsProvider metricsProvider) {
    this.detailService = detailService;
    this.padPipelineService = padPipelineService;
    this.padFileService = padFileService;
    this.pwaApplicationPermissionService = pwaApplicationPermissionService;
    this.metricsProvider = metricsProvider;
  }

  /**
   * Construct an application context to provide common objects associated with a PWA application and perform standard permission checks.
   * @return application context if app is in right state and user has right privileges, throw relevant exceptions otherwise
   */
  public PwaApplicationContext validateAndCreate(PwaApplicationContextParams contextParams) {

    var stopwatch = Stopwatch.createStarted();

    var applicationId = contextParams.getApplicationId();
    var context = getApplicationContext(applicationId, contextParams.getAuthenticatedUserAccount());

    ApplicationContextUtils.performAppStatusCheck(contextParams.getStatuses(), context.getApplicationDetail());
    performApplicationTypeCheck(contextParams.getTypes(), context.getApplicationType(), applicationId);
    performPermissionCheck(
        contextParams,
        context,
        contextParams.getAuthenticatedUserAccount(),
        applicationId);

    if (contextParams.getPadPipelineId() != null) {
      getAndSetPipeline(context, contextParams.getPadPipelineId());
    }

    if (contextParams.getFileId() != null) {
      getAndSetPadFile(context, contextParams.getFileId());
    }

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getAppContextTimer(), "Application Context created.");
    return context;

  }

  /**
   * Construct an application context to provide common objects associated with a PWA application.
   * @param applicationId for the PWA application
   * @param authenticatedUser trying to access the PWA application
   * @return application context object with app detail, users roles etc populated
   */
  private PwaApplicationContext getApplicationContext(Integer applicationId,
                                                      AuthenticatedUserAccount authenticatedUser) {

    var detail = detailService.getTipDetail(applicationId);

    var permissions = pwaApplicationPermissionService.getPermissions(detail, authenticatedUser.getLinkedPerson());

    return new PwaApplicationContext(detail, authenticatedUser, permissions);
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
   * If the user has ANY of the required permissions then pass, otherwise throw a relevant exception.
   */
  private void performPermissionCheck(PwaApplicationContextParams contextParams,
                                      PwaApplicationContext context,
                                      AuthenticatedUserAccount user,
                                      int applicationId) {

    var requiredPermissions = contextParams.getPermissions();

    if (!requiredPermissions.isEmpty()) {

      if (context.getPermissions().isEmpty()) {
        throwPermissionException(user.getWuaId(), applicationId, requiredPermissions);
      }

      boolean userHasRequiredPermissions = context.getPermissions().stream()
          .anyMatch(requiredPermissions::contains);

      if (!userHasRequiredPermissions) {
        throwPermissionException(user.getWuaId(), applicationId, requiredPermissions);
      }

    }

  }

  private void throwPermissionException(int wuaId,
                                        int applicationId,
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

    var padFile = padFileService.getPadFileByPwaApplicationDetailAndFileId(context.getApplicationDetail(), fileId);
    if (!Objects.equals(padFile.getPwaApplicationDetail(), context.getApplicationDetail())) {
      throw new AccessDeniedException(
          String.format("PadFile app detail (%s) didn't match the app context's app detail (%s)",
              padFile.getPwaApplicationDetail().getId(),
              context.getApplicationDetail().getId()));
    }
    context.setPadFile(padFile);

  }

}
