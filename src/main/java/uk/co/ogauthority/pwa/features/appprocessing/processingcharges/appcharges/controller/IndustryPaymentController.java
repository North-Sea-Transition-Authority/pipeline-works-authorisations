package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.CreatePaymentAttemptResult;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/app-payment")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.PAY_FOR_APPLICATION)
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT)
public class IndustryPaymentController {

  private static final String PAY_FOR_APP_LANDING_PAGE = "Pay for application";
  private static final String PAYMENT_ALREADY_COMPLETE_FLASH_TITLE = "Application %s has completed payment";
  private static final String PAYMENT_ALREADY_COMPLETE_FLASH_CONTENT =
      "You cannot pay for an application that has previously been paid for.";

  private final ApplicationBreadcrumbService breadcrumbService;
  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationPaymentSummariser applicationPaymentSummariser;
  private final PwaHolderService pwaHolderService;
  private final AnalyticsService analyticsService;

  private final String appBaseUrl;

  @Value("${context-path}")
  private String contextPath;

  @Autowired
  public IndustryPaymentController(ApplicationBreadcrumbService breadcrumbService,
                                   ApplicationChargeRequestService applicationChargeRequestService,
                                   ApplicationPaymentSummariser applicationPaymentSummariser,
                                   PwaHolderService pwaHolderService,
                                   @Value("${pwa.url.base}") String pwaUrlBase,
                                   @Value("${context-path}") String contextPath,
                                   AnalyticsService analyticsService) {
    this.breadcrumbService = breadcrumbService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.pwaHolderService = pwaHolderService;
    this.analyticsService = analyticsService;
    this.appBaseUrl = pwaUrlBase + contextPath;
  }

  @GetMapping
  public ModelAndView renderPayForApplicationLanding(@PathVariable("applicationId") Integer applicationId,
                                                     @PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     PwaAppProcessingContext processingContext) {
    return withAppChargeRequestReportOrError(
        processingContext,
        applicationChargeRequestReport -> getLandingPageModelAndView(processingContext, applicationChargeRequestReport)
    );

  }

  @PostMapping
  @PwaAppProcessingPermissionCheck(permissions = {
      PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY, PwaAppProcessingPermission.PAY_FOR_APPLICATION
  })
  @PwaApplicationStatusCheck(statuses = {
      PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT, PwaApplicationStatus.CASE_OFFICER_REVIEW
  })
  public ModelAndView startPaymentAttempt(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext,
                                          RedirectAttributes redirectAttributes,
                                          @CookieValue(name = "pwa-ga-client-id", required = false) Optional<String> analyticsClientId) {

    var startPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        processingContext.getPwaApplication(),
        processingContext.getUser());

    if (CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS == startPaymentAttemptResult.getPaymentAttemptOutcome()) {
      FlashUtils.info(
          redirectAttributes,
          String.format(PAYMENT_ALREADY_COMPLETE_FLASH_TITLE, processingContext.getPwaApplication().getAppReference()),
          PAYMENT_ALREADY_COMPLETE_FLASH_CONTENT
      );
      return CaseManagementUtils.redirectCaseManagement(processingContext);
    }

    analyticsService.sendGoogleAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.PAYMENT_ATTEMPT_STARTED);

    return new ModelAndView("redirect:" + startPaymentAttemptResult.getStartExternalJourneyUrl());

  }

  private ModelAndView getLandingPageModelAndView(PwaAppProcessingContext processingContext,
                                                  ApplicationChargeRequestReport applicationChargeRequestReport) {

    var appPaymentDisplaySummary = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

    var pwaHolderOrgNames = pwaHolderService.getPwaHolderOrgGroups(processingContext.getPwaApplication().getMasterPwa())
        .stream()
        .map(PortalOrganisationGroup::getName)
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(toList());

    var modelAndView = new ModelAndView("appprocessing/processingcharges/payForApplicationLanding")
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("appPaymentDisplaySummary", appPaymentDisplaySummary)
        .addObject("paymentLandingPageUrl", landingPageRoute(processingContext))
        .addObject("pwaHolderOrgNames", pwaHolderOrgNames)
        .addObject("financeRoleName", PwaOrganisationRole.FINANCE_ADMIN.getDisplayName())
        .addObject("errorList", List.of());

    breadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, PAY_FOR_APP_LANDING_PAGE);
    return modelAndView;

  }

  private String landingPageRoute(PwaAppProcessingContext processingContext) {
    return appBaseUrl + ReverseRouter.route(on(IndustryPaymentController.class).renderPayForApplicationLanding(
        processingContext.getMasterPwaApplicationId(),
        processingContext.getApplicationType(),
        null
    ));
  }

  private ModelAndView withAppChargeRequestReportOrError(PwaAppProcessingContext processingContext,
                                                         Function<ApplicationChargeRequestReport, ModelAndView> modelAndViewSupplier) {
    return applicationChargeRequestService.getOpenRequestAsApplicationChargeRequestReport(processingContext.getPwaApplication())
        .map(modelAndViewSupplier::apply)
        .orElseThrow(() -> new PwaEntityNotFoundException(
                "Could not locate application charge request for app Id:" + processingContext.getMasterPwaApplicationId()
            )
        );
  }

}
