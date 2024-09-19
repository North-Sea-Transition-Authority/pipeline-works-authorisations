package uk.co.ogauthority.pwa.features.application.submission.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.base.Stopwatch;
import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.controller.appsummary.ApplicationPipelineDataMapGuidanceController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.submission.PwaApplicationSubmissionService;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSummaryViewService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestViewService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.submission.ReviewAndSubmitApplicationForm;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaAppNotificationBannerService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.validators.appprocessing.PwaApplicationValidationService;
import uk.co.ogauthority.pwa.validators.pwaapplications.shared.submission.ReviewAndSubmitApplicationFormValidator;

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
public class ReviewAndSubmitController {

  private final ControllerHelperService controllerHelperService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PwaApplicationSubmissionService pwaApplicationSubmissionService;
  private final ApplicationSummaryViewService applicationSummaryViewService;
  private final ApplicationUpdateRequestViewService applicationUpdateRequestViewService;
  private final ReviewAndSubmitApplicationFormValidator validator;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final SendAppToSubmitterService sendAppToSubmitterService;
  private final PersonService personService;
  private final MetricsProvider metricsProvider;
  private final PwaAppNotificationBannerService pwaAppNotificationBannerService;
  private final PwaApplicationValidationService pwaApplicationValidationService;
  private final AnalyticsService analyticsService;

  private static final Logger LOGGER = LoggerFactory.getLogger(ReviewAndSubmitController.class);

  @Autowired
  public ReviewAndSubmitController(ControllerHelperService controllerHelperService,
                                   PwaApplicationRedirectService pwaApplicationRedirectService,
                                   PwaApplicationSubmissionService pwaApplicationSubmissionService,
                                   ApplicationSummaryViewService applicationSummaryViewService,
                                   ApplicationUpdateRequestViewService applicationUpdateRequestViewService,
                                   ReviewAndSubmitApplicationFormValidator validator,
                                   PwaHolderTeamService pwaHolderTeamService,
                                   SendAppToSubmitterService sendAppToSubmitterService,
                                   PersonService personService,
                                   MetricsProvider metricsProvider,
                                   PwaAppNotificationBannerService pwaAppNotificationBannerService,
                                   PwaApplicationValidationService pwaApplicationValidationService,
                                   AnalyticsService analyticsService) {
    this.controllerHelperService = controllerHelperService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.pwaApplicationSubmissionService = pwaApplicationSubmissionService;
    this.applicationSummaryViewService = applicationSummaryViewService;
    this.applicationUpdateRequestViewService = applicationUpdateRequestViewService;
    this.validator = validator;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.sendAppToSubmitterService = sendAppToSubmitterService;
    this.personService = personService;
    this.metricsProvider = metricsProvider;
    this.pwaAppNotificationBannerService = pwaAppNotificationBannerService;
    this.pwaApplicationValidationService = pwaApplicationValidationService;
    this.analyticsService = analyticsService;
  }

  @GetMapping
  public ModelAndView review(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                             @PathVariable("applicationId") int applicationId,
                             PwaApplicationContext applicationContext,
                             @ModelAttribute("form") ReviewAndSubmitApplicationForm form) {
    var applicationDetail = applicationContext.getApplicationDetail();
    if (Set.of(PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED).contains(applicationDetail.getStatus())) {
      return getModelAndView(applicationContext, form);
    }
    return CaseManagementUtils.redirectCaseManagement(applicationContext.getPwaApplication());
  }

  private ModelAndView getModelAndView(PwaApplicationContext applicationContext, ReviewAndSubmitApplicationForm form) {

    var detail = applicationContext.getApplicationDetail();
    var appSummaryView = applicationSummaryViewService.getApplicationSummaryView(detail);
    var isApplicationValid = pwaApplicationValidationService.isApplicationValid(detail);

    var modelAndView = new ModelAndView("pwaApplication/shared/submission/reviewAndSubmit")
        .addObject("appSummaryView", appSummaryView)
        .addObject("taskListUrl", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()))
        .addObject("applicationReference", detail.getPwaApplicationRef())
        .addObject("openUpdateRequest", false)
        .addObject("submitterCandidates", Map.of())
        .addObject("form", form)
        .addObject("userPermissions", applicationContext.getPermissions())
        .addObject("submitUrl", ReverseRouter.route(on(ReviewAndSubmitController.class)
            .submit(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, Optional.empty())))
        .addObject("mappingGuidanceUrl", ReverseRouter.route(on(ApplicationPipelineDataMapGuidanceController.class)
            .renderMappingGuidance(detail.getMasterPwaApplicationId(), detail.getPwaApplicationType(), null)))
        .addObject("showDiffCheckbox", true)
        .addObject("isApplicationValid", isApplicationValid);

    pwaAppNotificationBannerService.addParallelPwaApplicationsWarningBannerIfRequired(applicationContext.getPwaApplication(),
        modelAndView);

    // if there's an open update request, include it
    applicationUpdateRequestViewService.getOpenRequestView(detail.getPwaApplication())
        .ifPresent(view -> modelAndView
            .addObject("updateRequestView", view)
            .addObject("openUpdateRequest", true));

    // if user is not a submitter, get the list of submitters we can send to
    if (!applicationContext.getPermissions().contains(PwaApplicationPermission.SUBMIT)) {

      var submitterSelectOptions = pwaHolderTeamService
          .getPeopleWithHolderTeamRole(detail, PwaOrganisationRole.APPLICATION_SUBMITTER)
          .stream()
          .sorted(Comparator.comparing(Person::getFullName))
          .collect(StreamUtils.toLinkedHashMap(person -> String.valueOf(person.getId().asInt()), Person::getFullName));

      modelAndView.addObject("submitterCandidates", submitterSelectOptions);
      modelAndView.addObject("submitUrl", ReverseRouter.route(on(ReviewAndSubmitController.class)
          .sendToSubmitter(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null, null, null, null)));

    }

    return modelAndView;

  }

  @PostMapping
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.SUBMIT)
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView submit(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
                             @PathVariable("applicationId") int applicationId,
                             PwaApplicationContext applicationContext,
                             @ModelAttribute("form") ReviewAndSubmitApplicationForm form,
                             BindingResult bindingResult,
                             @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false)
                             Optional<String> analyticsClientId) {

    var stopwatch = Stopwatch.createStarted();

    var modelAndView = whenSubmittableCheckErrorsAndRedirect(
        applicationContext,
        form,
        bindingResult,
        getModelAndView(applicationContext, form),
        () -> {

          var detail = applicationContext.getApplicationDetail();

          if (!pwaApplicationValidationService.isApplicationValid(detail)) {
            return review(applicationType, applicationId, applicationContext, null);
          }

          var submissionType = pwaApplicationSubmissionService.submitApplication(
              applicationContext.getUser(),
              detail,
              BooleanUtils.isFalse(form.getMadeOnlyRequestedChanges()) ? form.getOtherChangesDescription() : null
          );

          analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.APPLICATION_SUBMISSION,
              Map.of("submissionType", submissionType.name(),
                  "applicationType", detail.getPwaApplicationType().name()));

          return ReverseRouter.redirect(
              on(SubmitConfirmationController.class).confirmSubmission(applicationType, applicationId, null));

        }

    );

    MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getAppSubmissionTimer(), "Application submitted.");
    return modelAndView;
  }

  @PostMapping("/send-to-submitter")
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView sendToSubmitter(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") int applicationId,
      PwaApplicationContext applicationContext,
      @ModelAttribute("form") ReviewAndSubmitApplicationForm form,
      BindingResult bindingResult,
      HttpSession session) {

    return whenSubmittableCheckErrorsAndRedirect(
        applicationContext,
        form,
        bindingResult,
        getModelAndView(applicationContext, form),
        () -> {

          var submitterPerson = personService.getPersonById(new PersonId(form.getSubmitterPersonId()));

          sendAppToSubmitterService.sendToSubmitter(
              applicationContext.getApplicationDetail(),
              applicationContext.getUser().getLinkedPerson(),
              BooleanUtils.isFalse(form.getMadeOnlyRequestedChanges()) ? form.getOtherChangesDescription() : null,
              submitterPerson);

          // add to session so confirmation screen can access without worrying about losing the flash scope
          // after a page reload.
          session.setAttribute("submitterPersonName", submitterPerson.getFullName());

          return ReverseRouter.redirect(on(SubmitConfirmationController.class)
              .confirmSentToSubmitter(applicationType, applicationId, null, null));

        }

    );

  }

  private ModelAndView whenSubmittableCheckErrorsAndRedirect(PwaApplicationContext applicationContext,
                                                             ReviewAndSubmitApplicationForm form,
                                                             BindingResult bindingResult,
                                                             ModelAndView modelAndView,
                                                             Supplier<ModelAndView> ifValid) {
    if (!pwaApplicationValidationService.isApplicationValid(applicationContext.getApplicationDetail())) {
      return review(applicationContext.getApplicationType(), applicationContext.getPwaApplication().getId(), applicationContext, null);
    }

    validator.validate(form, bindingResult, applicationContext);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, ifValid);
  }

}
