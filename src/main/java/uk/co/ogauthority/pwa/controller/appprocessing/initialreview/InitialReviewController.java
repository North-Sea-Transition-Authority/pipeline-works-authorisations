package uk.co.ogauthority.pwa.controller.appprocessing.initialreview;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.initialreview.InitialReviewForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.initialreview.InitialReviewService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
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
  private final WorkflowAssignmentService workflowAssignmentService;
  private final InitialReviewFormValidator initialReviewFormValidator;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public InitialReviewController(ApplicationBreadcrumbService breadcrumbService,
                                 InitialReviewService initialReviewService,
                                 WorkflowAssignmentService workflowAssignmentService,
                                 InitialReviewFormValidator initialReviewFormValidator,
                                 ControllerHelperService controllerHelperService) {
    this.breadcrumbService = breadcrumbService;
    this.initialReviewService = initialReviewService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.initialReviewFormValidator = initialReviewFormValidator;
    this.controllerHelperService = controllerHelperService;
  }

  private ModelAndView getInitialReviewModelAndView(PwaApplicationDetail detail) {

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/initialReview/initialReview")
        .addObject("appRef", detail.getPwaApplicationRef())
        .addObject("isOptionsVariation", detail.getPwaApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION))
        .addObject("isFastTrack", detail.getSubmittedAsFastTrackFlag())
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null)))
        .addObject("caseOfficerCandidates",
            workflowAssignmentService
                .getAssignmentCandidates(detail.getPwaApplication(), PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW).stream()
                .sorted(Comparator.comparing(Person::getFullName))
                .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()),
                    Person::getFullName)));

    breadcrumbService.fromWorkArea(modelAndView, detail.getPwaApplicationRef());

    return modelAndView;

  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
  public ModelAndView renderInitialReview(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          @ModelAttribute("form") InitialReviewForm form,
                                          AuthenticatedUserAccount user) {
    return getInitialReviewModelAndView(processingContext.getApplicationDetail());
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

    initialReviewFormValidator.validate(form, bindingResult, processingContext.getPwaApplication());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getInitialReviewModelAndView(processingContext.getApplicationDetail()),
        () -> {

          try {
            initialReviewService.acceptApplication(processingContext.getApplicationDetail(),
                form.getCaseOfficerPersonId(), user);
            FlashUtils.success(redirectAttributes,
                "Accepted initial review for " + processingContext.getApplicationDetail().getPwaApplicationRef());
          } catch (ActionAlreadyPerformedException e) {
            FlashUtils.error(redirectAttributes, String.format("Initial review for %s already accepted",
                processingContext.getApplicationDetail().getPwaApplicationRef()));
          }

          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null));

        });

  }

}
