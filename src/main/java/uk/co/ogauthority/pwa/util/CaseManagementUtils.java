package uk.co.ogauthority.pwa.util;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.appprocessing.CaseManagementController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tabs.AppProcessingTab;

public class CaseManagementUtils {

  private CaseManagementUtils() {
    throw new AssertionError();
  }

  public static String routeCaseManagement(PwaApplication pwaApplication) {

    return ReverseRouter.route(on(CaseManagementController.class)
        .renderCaseManagement(pwaApplication.getId(), pwaApplication.getApplicationType(), AppProcessingTab.TASKS, null, null));

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

}
