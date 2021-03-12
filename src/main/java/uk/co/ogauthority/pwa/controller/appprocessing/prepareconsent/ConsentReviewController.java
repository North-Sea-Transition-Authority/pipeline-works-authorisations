package uk.co.ogauthority.pwa.controller.appprocessing.prepareconsent;

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
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.exception.appprocessing.ConsentReviewException;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent.ConsentReviewReturnForm;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewReturnFormValidator;
import uk.co.ogauthority.pwa.service.appprocessing.consentreview.ConsentReviewService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
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

  @Autowired
  public ConsentReviewController(AppProcessingBreadcrumbService breadcrumbService,
                                 ControllerHelperService controllerHelperService,
                                 ConsentReviewService consentReviewService,
                                 PwaTeamService pwaTeamService,
                                 AssignmentService assignmentService,
                                 ConsentReviewReturnFormValidator consentReviewReturnFormValidator,
                                 PersonService personService) {
    this.breadcrumbService = breadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.consentReviewService = consentReviewService;
    this.pwaTeamService = pwaTeamService;
    this.assignmentService = assignmentService;
    this.consentReviewReturnFormValidator = consentReviewReturnFormValidator;
    this.personService = personService;
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

    var caseOfficerOptions = pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.CASE_OFFICER).stream()
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

    var modelAndView = new ModelAndView("pwaApplication/appProcessing/prepareConsent/issueConsent")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("cancelUrl", cancelUrl);

    breadcrumbService.fromPrepareConsent(processingContext.getPwaApplication(), modelAndView, "Issue consent");

    return modelAndView;

  }

  @PostMapping("/issue")
  public ModelAndView issueConsent(@PathVariable("applicationId") Integer applicationId,
                                   @PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   PwaAppProcessingContext processingContext,
                                   AuthenticatedUserAccount authenticatedUserAccount,
                                   RedirectAttributes redirectAttributes) {

    try {

      consentReviewService.issueConsent(processingContext.getApplicationDetail(), authenticatedUserAccount);

      FlashUtils.success(redirectAttributes, processingContext.getPwaApplication().getAppReference() + " consent issued");

    } catch (ConsentReviewException e) {

      FlashUtils.error(redirectAttributes, processingContext.getPwaApplication().getAppReference() + " consent not issued",
          "Consent review no longer open, someone else may have already performed this action.");

    }

    return RouteUtils.redirectWorkArea();

  }

}
