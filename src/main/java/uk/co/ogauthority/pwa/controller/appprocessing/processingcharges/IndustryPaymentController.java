package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.CreatePaymentAttemptResult;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.display.ApplicationPaymentSummariser;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaOrganisationUserRole;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
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
  // TODO PWA-1148 - replace with dedicated pwa holder org service
  private final PwaHolderTeamService pwaHolderTeamService;

  private final String appBaseUrl;

  @Value("${context-path}")
  private String contextPath;

  @Autowired
  public IndustryPaymentController(ApplicationBreadcrumbService breadcrumbService,
                                   ApplicationChargeRequestService applicationChargeRequestService,
                                   ApplicationPaymentSummariser applicationPaymentSummariser,
                                   PwaHolderTeamService pwaHolderTeamService,
                                   @Value("${pwa.url.base}") String pwaUrlBase,
                                   @Value("${context-path}") String contextPath) {
    this.breadcrumbService = breadcrumbService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationPaymentSummariser = applicationPaymentSummariser;
    this.pwaHolderTeamService = pwaHolderTeamService;
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
                                          RedirectAttributes redirectAttributes) {

    var startPaymentAttemptResult = applicationChargeRequestService.startChargeRequestPaymentAttempt(
        processingContext.getPwaApplication(),
        processingContext.getUser());

    if (CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS.equals(
        startPaymentAttemptResult.getPaymentAttemptOutcome())) {
      FlashUtils.info(
          redirectAttributes,
          String.format(PAYMENT_ALREADY_COMPLETE_FLASH_TITLE, processingContext.getPwaApplication().getAppReference()),
          PAYMENT_ALREADY_COMPLETE_FLASH_CONTENT
      );
      return CaseManagementUtils.redirectCaseManagement(processingContext);
    }

    return new ModelAndView("redirect:" +
        applicationChargeRequestService.startChargeRequestPaymentAttempt(
            processingContext.getPwaApplication(),
            processingContext.getUser()
        )
            .getStartExternalJourneyUrl()
    );

  }

  private ModelAndView getLandingPageModelAndView(PwaAppProcessingContext processingContext,
                                                  ApplicationChargeRequestReport applicationChargeRequestReport) {

    var appPaymentDisplaySummary = applicationPaymentSummariser.summarise(applicationChargeRequestReport);

    var pwaHolderOrgNames = pwaHolderTeamService.getHolderOrgGroups(processingContext.getApplicationDetail())
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
        .addObject("financeRoleName", PwaOrganisationUserRole.FINANCE_ADMIN.getRoleName())
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
    return applicationChargeRequestService.getApplicationChargeRequestReport(processingContext.getPwaApplication())
        .map(modelAndViewSupplier::apply)
        .orElseThrow(() -> new PwaEntityNotFoundException(
                "Could not locate application charge request for app Id:" + processingContext.getMasterPwaApplicationId()
            )
        );
  }

}
