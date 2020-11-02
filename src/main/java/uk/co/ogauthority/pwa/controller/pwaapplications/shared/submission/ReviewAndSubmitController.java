package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/review")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
public class ReviewAndSubmitController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final ApplicationSummaryViewService applicationSummaryViewService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public ReviewAndSubmitController(PwaApplicationRedirectService pwaApplicationRedirectService,
                                   PwaApplicationSubmissionService pwaApplicationSubmissionService,
                                   ApplicationSummaryViewService applicationSummaryViewService,
                                   ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.applicationSummaryViewService = applicationSummaryViewService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @GetMapping
  public ModelAndView review(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext) {

    var modelAndView = new ModelAndView("pwaApplication/shared/submission/reviewAndSubmit");

    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(applicationContext.getApplicationDetail());

    modelAndView
        .addObject("appSummaryView", appSummaryView)
        .addObject("taskListUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("applicationReference", applicationContext.getPwaApplication().getAppReference());

    return modelAndView;

  }


  @PostMapping
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SUBMIT)
  public ModelAndView submit(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext) {

    pwaApplicationSubmissionService.submitApplication(applicationContext.getUser(),
        applicationContext.getApplicationDetail(), null);
    return ReverseRouter.redirect(
        on(SubmitConfirmationController.class).confirmation(applicationType, applicationId, null));

  }
}
