package uk.co.ogauthority.pwa.controller.appprocessing.initialreview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.form.appprocessing.initialreview.InitialReviewForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.initialreview.InitialReviewFormValidator;

@RequestMapping("/pwa-application-processing/{applicationType}/{applicationId}/initial-review")
@Controller
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW})
public class InitialReviewController {

  private final ApplicationBreadcrumbService breadcrumbService;
  private final InitialReviewService initialReviewService;
  private final ApplicationFeeService applicationFeeService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final InitialReviewFormValidator initialReviewFormValidator;
  private final ControllerHelperService controllerHelperService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public InitialReviewController(ApplicationBreadcrumbService breadcrumbService,
                                 InitialReviewService initialReviewService,
                                 ApplicationFeeService applicationFeeService,
                                 ApplicationPaymentSummariser applicationPaymentSummariser,
                                 WorkflowAssignmentService workflowAssignmentService,
                                 InitialReviewFormValidator initialReviewFormValidator,
                                 ControllerHelperService controllerHelperService,
                                 ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.breadcrumbService = breadcrumbService;
    this.initialReviewService = initialReviewService;
    this.applicationFeeService = applicationFeeService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.workflowAssignmentService = workflowAssignmentService;
    this.initialReviewFormValidator = initialReviewFormValidator;
    this.controllerHelperService = controllerHelperService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  private ModelAndView getInitialReviewModelAndView(PwaAppProcessingContext appProcessingContext) {

    var detail = appProcessingContext.getApplicationDetail();
    var feeReport = applicationFeeService.getApplicationFeeReport(appProcessingContext.getApplicationDetail());
    var displayableFees = applicationPaymentSummariser.summarise(feeReport);


    var modelAndView = new ModelAndView("pwaApplication/appProcessing/initialReview/initialReview")
        .addObject("appRef", detail.getPwaApplicationRef())
        .addObject("appPaymentDisplaySummary", displayableFees)
        .addObject("paymentDecisionOptions", Arrays.stream(InitialReviewPaymentDecision.values())
            .collect(StreamUtils.toLinkedHashMap(Enum::name, InitialReviewPaymentDecision::getDisplayText)))
        .addObject("isOptionsVariation", detail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION))
        .addObject("isFastTrack", detail.getSubmittedAsFastTrackFlag())
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(detail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)
                .stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)))
        .addObject("caseSummaryView", appProcessingContext.getCaseSummaryView())
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(appProcessingContext));

    breadcrumbService.fromWorkArea(modelAndView, detail.getPwaApplicationRef());

    return modelAndView;

  }

  private ModelAndView whenReviewable(PwaAppProcessingContext processingContext,
                                      Supplier<ModelAndView> modelAndViewSupplier) {

    if (applicationUpdateRequestService.applicationHasOpenUpdateRequest(processingContext.getApplicationDetail())) {
      throw new AccessDeniedException(String.format(
          "Can't access %s controller routes as application with id [%s] has an open update request",
          PwaAppProcessingTask.INITIAL_REVIEW.name(),
          processingContext.getMasterPwaApplicationId()));
    }

    return modelAndViewSupplier.get();

  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
  public ModelAndView renderInitialReview(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @ModelAttribute("form") InitialReviewForm form,
                                          AuthenticatedUserAccount user) {
    return whenReviewable(processingContext, () -> getInitialReviewModelAndView(processingContext));
  }

  @PostMapping
  public ModelAndView postInitialReview(@PathVariable("applicationId") Integer applicationId,
                                        @PathVariable("applicationType")
                                        @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                        PwaAppProcessingContext processingContext,
                                        @ModelAttribute("form") InitialReviewForm form,
                                        BindingResult bindingResult,
                                        AuthenticatedUserAccount user,
                                        RedirectAttributes redirectAttributes) {

    return whenReviewable(processingContext, () -> {

      initialReviewFormValidator.validate(form, bindingResult, processingContext.getPwaApplication());

      return controllerHelperService.checkErrorsAndRedirect(bindingResult,
          getInitialReviewModelAndView(processingContext),
          () -> {

            try {
              initialReviewService.acceptApplication(
                  processingContext.getApplicationDetail(),
                  new PersonId(form.getCaseOfficerPersonId()),
                  form.getInitialReviewPaymentDecision(),
                  InitialReviewPaymentDecision.PAYMENT_WAIVED.equals(form.getInitialReviewPaymentDecision())
                      ? form.getPaymentWaivedReason()
                      : null,
                  user);
              FlashUtils.success(redirectAttributes,
                  "Accepted initial review for " + processingContext.getApplicationDetail().getPwaApplicationRef());
            } catch (ActionAlreadyPerformedException e) {
              FlashUtils.error(redirectAttributes, String.format("Initial review for %s already accepted",
                  processingContext.getApplicationDetail().getPwaApplicationRef()));
            }

            return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));

          });

    });

  }

}
