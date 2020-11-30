package uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ApplicationUpdateResponseForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission.ApplicationUpdateResponseFormValidator;

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

  private final ControllerHelperService controllerHelperService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final ApplicationSummaryViewService applicationSummaryViewService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final ApplicationUpdateResponseFormValidator validator;

  @Autowired
  public ReviewAndSubmitController(
      ControllerHelperService controllerHelperService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PwaApplicationSubmissionService pwaApplicationSubmissionService,
      ApplicationSummaryViewService applicationSummaryViewService,
      ApplicationUpdateRequestService applicationUpdateRequestService,
      ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
      ApplicationUpdateResponseFormValidator validator) {
    this.controllerHelperService = controllerHelperService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.applicationSummaryViewService = applicationSummaryViewService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.validator = validator;
  }

  @GetMapping
  public ModelAndView review(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext,
      @ModelAttribute("form") ApplicationUpdateResponseForm form) {

    var hasOpenUpdateRequest = applicationUpdateRequestService
        .applicationDetailHasOpenUpdateRequest(applicationContext.getApplicationDetail());
    return getModelAndView(applicationContext.getApplicationDetail(), hasOpenUpdateRequest);

  }

  private ModelAndView getModelAndView(PwaApplicationDetail pwaApplicationDetail, boolean openUpdateRequest) {
    var modelAndView = new ModelAndView("pwaApplication/shared/submission/reviewAndSubmit");
    var openUpdateRequestViewOpt = applicationUpdateRequestViewService.getOpenRequestView(pwaApplicationDetail.getPwaApplication());
    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(pwaApplicationDetail);

    modelAndView
        .addObject("appSummaryView", appSummaryView)
        .addObject("taskListUrl",
            pwaApplicationRedirectService.getTaskListRoute(pwaApplicationDetail.getPwaApplication()))
        .addObject("applicationReference", pwaApplicationDetail.getPwaApplicationRef())
        .addObject("openUpdateRequest", openUpdateRequest);

    openUpdateRequestViewOpt.ifPresent(applicationUpdateRequestView ->
        modelAndView.addObject("updateRequestView", applicationUpdateRequestView)
    );

    return modelAndView;

  }


  @PostMapping
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SUBMIT)
  public ModelAndView submit(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext,
      @ModelAttribute("form") ApplicationUpdateResponseForm form,
      BindingResult bindingResult) {

    var hasOpenUpdateRequest = applicationUpdateRequestService
        .applicationDetailHasOpenUpdateRequest(applicationContext.getApplicationDetail());

    if (hasOpenUpdateRequest) {
      ValidationUtils.invokeValidator(validator, form, bindingResult);
    }

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        getModelAndView(applicationContext.getApplicationDetail(), hasOpenUpdateRequest),
        () -> {
          pwaApplicationSubmissionService.submitApplication(
              applicationContext.getUser(),
              applicationContext.getApplicationDetail(),
              BooleanUtils.isFalse(form.getMadeOnlyRequestedChanges()) ? form.getOtherChangesDescription() : null
          );

          return ReverseRouter.redirect(
              on(SubmitConfirmationController.class).confirmation(applicationType, applicationId, null));
        }

    );

  }
}
