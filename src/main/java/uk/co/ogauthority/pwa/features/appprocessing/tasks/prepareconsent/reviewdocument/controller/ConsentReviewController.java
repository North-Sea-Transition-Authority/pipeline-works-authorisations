package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.AppProcessingTaskWarningService;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.NonBlockingWarningPage;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.draftdocument.controller.AppConsentDocController;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewReturnForm;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewReturnFormValidator;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.reviewdocument.ConsentReviewService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.Assignment;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.AssignmentService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignment;
import uk.co.ogauthority.pwa.features.consents.viewconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.enums.consultations.ConsultationResponseDocumentType;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerBodyLine;
import uk.co.ogauthority.pwa.model.view.notificationbanner.NotificationBannerView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.RouteUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/consent-review")
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CONSENT_REVIEW)
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.CONSENT_REVIEW)
public class ConsentReviewController {

  private final AppProcessingBreadcrumbService breadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final ConsentReviewService consentReviewService;
  private final PwaTeamService pwaTeamService;
  private final AssignmentService assignmentService;
  private final ConsentReviewReturnFormValidator consentReviewReturnFormValidator;
  private final PersonService personService;
  private final AppProcessingTaskWarningService appProcessingTaskWarningService;
  private final ConsentFileViewerService consentFileViewerService;

  private final PadPipelineTransferService pipelineTransferService;

  @Autowired
  public ConsentReviewController(AppProcessingBreadcrumbService breadcrumbService,
                                 ControllerHelperService controllerHelperService,
                                 ConsentReviewService consentReviewService,
                                 PwaTeamService pwaTeamService,
                                 AssignmentService assignmentService,
                                 ConsentReviewReturnFormValidator consentReviewReturnFormValidator,
                                 PersonService personService,
                                 AppProcessingTaskWarningService appProcessingTaskWarningService,
                                 ConsentFileViewerService consentFileViewerService,
                                 PadPipelineTransferService pipelineTransferService) {
    this.breadcrumbService = breadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.consentReviewService = consentReviewService;
    this.pwaTeamService = pwaTeamService;
    this.assignmentService = assignmentService;
    this.consentReviewReturnFormValidator = consentReviewReturnFormValidator;
    this.personService = personService;
    this.appProcessingTaskWarningService = appProcessingTaskWarningService;
    this.consentFileViewerService = consentFileViewerService;
    this.pipelineTransferService = pipelineTransferService;
  }

  @GetMapping("/return")
  public ModelAndView renderReturnToCaseOfficer(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                @ModelAttribute("form") ConsentReviewReturnForm form,
                                                AuthenticatedUserAccount authenticatedUserAccount) {
    return getReturnToCaseOfficerModelAndView(processingContext, form);
  }

  private ModelAndView getReturnToCaseOfficerModelAndView(PwaAppProcessingContext processingContext,
                                                          ConsentReviewReturnForm form) {

    var caseOfficerOptions = pwaTeamService.getPeopleWithRegulatorRole(Role.CASE_OFFICER).stream()
        .sorted(Comparator.comparing(Person::getFullName))
        .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()), Person::getFullName));

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/returnToCaseOfficer")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("caseOfficerOptions", caseOfficerOptions)
        .addObject("cancelUrl", ReverseRouter.route(on(AppConsentDocController.class)
            .renderConsentDocEditor(processingContext.getMasterPwaApplicationId(), processingContext.getApplicationType(), null, null)));

    // if no-one selected as CO on form and assigned CO is still available as a CO, preload them in the form
    if (form.getCaseOfficerPersonId() == null) {

      assignmentService.getAssignments(processingContext.getPwaApplication()).stream()
          .filter(ass -> ass.getWorkflowAssignment().equals(WorkflowAssignment.CASE_OFFICER))
          .map(Assignment::getAssigneePersonId)
          .filter(assigneePersonId -> caseOfficerOptions.containsKey(String.valueOf(assigneePersonId.asInt())))
          .findFirst()
          .ifPresent(assigneePersonId -> form.setCaseOfficerPersonId(assigneePersonId.asInt()));

    }

    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, "Return to case officer");

    return modelAndView;

  }

  @PostMapping("/return")
  public ModelAndView returnToCaseOfficer(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      @ModelAttribute("form") ConsentReviewReturnForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount authUser,
                                      RedirectAttributes redirectAttributes) {

    consentReviewReturnFormValidator.validate(form, bindingResult, processingContext.getPwaApplication());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getReturnToCaseOfficerModelAndView(processingContext, form), () -> {

          var caseOfficerPerson = personService.getPersonById(new PersonId(form.getCaseOfficerPersonId()));

          try {

            consentReviewService.returnToCaseOfficer(processingContext.getApplicationDetail(), form.getReturnReason(),
                caseOfficerPerson, authUser);

            FlashUtils.info(redirectAttributes,
                processingContext.getPwaApplication().getAppReference() + " consent returned to case officer");

          } catch (ConsentReviewException e) {

            FlashUtils.error(redirectAttributes,
                processingContext.getPwaApplication().getAppReference() + " consent not returned",
                "Consent review no longer open, someone else may have already performed this action.");

          }

          return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));

        });

  }

  @GetMapping("/issue")
  public ModelAndView renderIssueConsent(@PathVariable("applicationId") Integer applicationId,
                                         @PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         PwaAppProcessingContext processingContext,
                                         AuthenticatedUserAccount authenticatedUserAccount) {

    var cancelUrl = ReverseRouter.route(on(AppConsentDocController.class)
        .renderConsentDocEditor(applicationId, pwaApplicationType, null, null));

    var sosdConsultationRequestView = consentFileViewerService.getLatestConsultationRequestViewForDocumentType(
        processingContext.getPwaApplication(), ConsultationResponseDocumentType.SECRETARY_OF_STATE_DECISION).orElse(null);

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/issueConsent")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("cancelUrl", cancelUrl)
        .addObject("consentTransferBlock", false)
        .addObject("nonBlockingTasksWarning",
            appProcessingTaskWarningService.getNonBlockingTasksWarning(processingContext.getPwaApplication(),
                NonBlockingWarningPage.ISSUE_CONSENT))
        .addObject("sosdConsultationRequestView", sosdConsultationRequestView);

    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, "Issue consent");

    // locked for transfers not yet completed
    var transfers = pipelineTransferService.findByRecipientApplication(processingContext.getApplicationDetail())
        .stream()
        .filter(transfer -> !transfer.getDonorApplicationDetail().getStatus().equals(PwaApplicationStatus.COMPLETE))
        .collect(Collectors.toList());
    if (!transfers.isEmpty()) {
      var bodyLine = new NotificationBannerBodyLine(
          "The consent cannot be issued until the following applications have been consented first", null);
      var pipelineTransferBannerBuilder = new NotificationBannerView.BannerBuilder("Awaiting transfer completion")
          .addBodyLine(bodyLine);

      for (var transfer : transfers) {
        var transferLine = new NotificationBannerBodyLine(
            transfer.getDonorApplicationDetail().getPwaApplicationRef(),
            "govuk-!-font-weight-bold govuk-list--bullet"
        );
        pipelineTransferBannerBuilder.addBodyLine(transferLine);
        modelAndView.addObject("pipelineTransferPageBannerView", pipelineTransferBannerBuilder.build());
        modelAndView.addObject("consentTransferBlock", true);
      }
    }

    return modelAndView;

  }

  @PostMapping("/issue")
  public ModelAndView scheduleConsentIssue(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           AuthenticatedUserAccount authenticatedUserAccount,
                                           RedirectAttributes redirectAttributes) {
    // locked for transfers not yet completed
    var transfers = pipelineTransferService.findByRecipientApplication(processingContext.getApplicationDetail())
        .stream()
        .filter(transfer -> !transfer.getDonorApplicationDetail().getStatus().equals(PwaApplicationStatus.COMPLETE))
        .collect(Collectors.toList());
    if (!transfers.isEmpty()) {
      return ReverseRouter.redirect(on(ConsentReviewController.class).renderIssueConsent(
          applicationId,
          pwaApplicationType,
          processingContext,
          authenticatedUserAccount
      ));
    }

    try {

      consentReviewService.scheduleConsentIssue(processingContext.getApplicationDetail(), authenticatedUserAccount);

      FlashUtils.success(redirectAttributes,
          String.format(
              "Consent issue has been scheduled for application %s",
              processingContext.getPwaApplication().getAppReference())
      );

    } catch (ConsentReviewException e) {

      FlashUtils.error(redirectAttributes, processingContext.getPwaApplication().getAppReference() + " consent issue not scheduled",
          "Consent review no longer open, someone else may have already performed this action.");

    }

    return RouteUtils.redirectWorkArea();

  }

}
