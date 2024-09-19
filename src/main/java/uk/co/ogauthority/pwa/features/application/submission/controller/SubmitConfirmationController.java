package uk.co.ogauthority.pwa.features.application.submission.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.submission.ApplicationSummaryFactory;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/submit-confirmation")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.HUOO_VARIATION
})
public class SubmitConfirmationController {

  private final ApplicationSummaryFactory applicationSummaryFactory;

  @Autowired
  public SubmitConfirmationController(ApplicationSummaryFactory applicationSummaryFactory) {
    this.applicationSummaryFactory = applicationSummaryFactory;
  }

  @GetMapping("/submitted")
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SUBMIT)
  public ModelAndView confirmSubmission(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType applicationType,
                                        @PathVariable("applicationId") int applicationId,
                                        PwaApplicationContext applicationContext) {

    var submissionSummary = applicationSummaryFactory.createSubmissionSummary(
        applicationContext.getApplicationDetail());

    return new ModelAndView("pwaApplication/shared/submission/submitConfirmation")
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("submissionSummary", submissionSummary)
        .addObject("isFirstVersion", applicationContext.getApplicationDetail().isFirstVersion())
        .addObject("feedbackUrl", ControllerUtils.getFeedbackUrl(applicationContext.getApplicationDetail().getId()));

  }

  @GetMapping("/sent-to-submitter")
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
  public ModelAndView confirmSentToSubmitter(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType applicationType,
                                             @PathVariable("applicationId") int applicationId,
                                             PwaApplicationContext applicationContext,
                                             HttpSession session) {

    var submitterPersonName = (String) session.getAttribute("submitterPersonName");

    return new ModelAndView("pwaApplication/shared/submission/sentToSubmitter")
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("isFirstVersion", applicationContext.getApplicationDetail().isFirstVersion())
        .addObject("submitterPersonName", submitterPersonName)
        .addObject("applicationReference", applicationContext.getPwaApplication().getAppReference())
        .addObject("feedbackUrl", ControllerUtils.getFeedbackUrl(applicationContext.getApplicationDetail().getId()));

  }

}
