package uk.co.ogauthority.pwa.controller.appsummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/summary")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.VIEW_APPLICATION_SUMMARY)
public class ApplicationSummaryController {

  private final ApplicationSummaryViewService applicationSummaryViewService;

  @Autowired
  public ApplicationSummaryController(ApplicationSummaryViewService applicationSummaryViewService) {
    this.applicationSummaryViewService = applicationSummaryViewService;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationId") Integer applicationId,
                                    @PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    PwaAppProcessingContext processingContext,
                                    AuthenticatedUserAccount authenticatedUserAccount) {

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/appSummary/viewAppSummary");

    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(processingContext.getApplicationDetail());

    modelAndView
        .addObject("appSummaryView", appSummaryView)
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    return modelAndView;

  }

}
