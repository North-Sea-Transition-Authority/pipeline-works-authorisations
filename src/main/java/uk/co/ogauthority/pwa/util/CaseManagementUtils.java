package uk.co.ogauthority.pwa.util;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.summary.controller.ApplicationSummaryController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;

public class CaseManagementUtils {

  private CaseManagementUtils() {
    throw new AssertionError();
  }

  public static String routeApplicationSummary(Integer pwaApplicationId, PwaApplicationType applicationType) {

    return ReverseRouter.route(on(ApplicationSummaryController.class)
        .renderSummary(pwaApplicationId, applicationType, null, null, null, null));
  }

  public static String routeCaseManagement(Integer pwaApplicationId, PwaApplicationType applicationType) {

    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(pwaApplicationId, applicationType, AppProcessingTab.TASKS, null, null));
  }

  public static String routeCaseManagement(PwaApplication pwaApplication) {
    return routeCaseManagement(pwaApplication.getId(), pwaApplication.getApplicationType());
  }

  public static String routeCaseManagement(PwaAppProcessingContext processingContext) {
    return routeCaseManagement(processingContext.getPwaApplication());
  }

  public static ModelAndView redirectCaseManagement(PwaApplication pwaApplication) {

    return ReverseRouter.redirect(on(CaseManagementController.class)
        .renderCaseManagement(pwaApplication.getId(), pwaApplication.getApplicationType(), AppProcessingTab.TASKS, null, null));

  }

  public static ModelAndView redirectCaseManagement(PwaAppProcessingContext processingContext) {
    return redirectCaseManagement(processingContext.getPwaApplication());
  }

  public static ModelAndView withAtLeastOneSatisfactoryVersion(PwaAppProcessingContext processingContext,
                                                               PwaAppProcessingTask processingTask,
                                                               Supplier<ModelAndView> modelAndViewSupplier) {

    if (!processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion()) {
      throw new AccessDeniedException(String.format(
          "Can't access %s controller routes as application with id [%s] has no satisfactory versions",
          processingTask.name(),
          processingContext.getMasterPwaApplicationId()));
    }

    return modelAndViewSupplier.get();

  }

  public static ResponseEntity<Resource> resourceWithAtLeastOneSatisfactoryVersion(PwaAppProcessingContext processingContext,
                                                                                   PwaAppProcessingTask processingTask,
                                                                                   Supplier<ResponseEntity<Resource>> resourceSupplier) {

    if (!processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion()) {
      throw new AccessDeniedException(String.format(
          "Can't access %s controller routes as application with id [%s] has no satisfactory versions",
          processingTask.name(),
          processingContext.getMasterPwaApplicationId()));
    }

    return resourceSupplier.get();

  }

}
