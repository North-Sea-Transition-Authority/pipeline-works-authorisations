package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeException;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ProcessPaymentAttemptOutcome;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
public class IndustryPaymentCallbackController {

  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final AnalyticsService analyticsService;

  @Autowired
  public IndustryPaymentCallbackController(ApplicationChargeRequestService applicationChargeRequestService,
                                           AnalyticsService analyticsService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.analyticsService = analyticsService;
  }

  @GetMapping("/pwa-application/pay/return/{paymentJourneyUuid}")
  public ModelAndView reconcilePaymentRequestAndRedirect(@PathVariable("paymentJourneyUuid") UUID paymentJourneyUuid,
                                                         AuthenticatedUserAccount user,
                                                         RedirectAttributes redirectAttributes,
                                                         @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false)
                                                               Optional<String> analyticsClientId) {
    var reconciledPaymentAttempt = applicationChargeRequestService
        .reconcilePaymentRequestCallbackUuidToPaymentAttempt(paymentJourneyUuid);

    var pwaApplication = reconciledPaymentAttempt.getPwaAppChargeRequest().getPwaApplication();

    var processPaymentAttemptOutcome = applicationChargeRequestService.processPaymentAttempt(
        reconciledPaymentAttempt,
        user
    );

    if (processPaymentAttemptOutcome.equals(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_PAID)) {

      analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.PAYMENT_ATTEMPT_COMPLETED);

      return ReverseRouter.redirect(on(this.getClass())
          .renderPaymentResult(pwaApplication.getId(), pwaApplication.getApplicationType(), null));

    } else {

      analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.PAYMENT_ATTEMPT_NOT_COMPLETED);

      FlashUtils.info(redirectAttributes,
          String.format("No payment for application %s has been completed", pwaApplication.getAppReference()));

      return CaseManagementUtils.redirectCaseManagement(pwaApplication);

    }

  }

  @GetMapping("/pwa-application/{applicationType}/{applicationId}/payment-result")
  @PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
  public ModelAndView renderPaymentResult(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaAppProcessingContext processingContext) {
    return withPaidAppChargeRequestReportOrError(
        processingContext,
        applicationChargeRequestReport -> getPaymentCompleteModelAndView(processingContext)
    );

  }

  private ModelAndView getPaymentCompleteModelAndView(PwaAppProcessingContext processingContext) {

    return new ModelAndView("appprocessing/processingcharges/paymentCompleteConfirmation")
        .addObject("appRef", processingContext.getPwaApplication().getAppReference())
        .addObject("workAreaUrl", ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null)))
        .addObject("caseManagementUrl", CaseManagementUtils.routeCaseManagement(processingContext));

  }

  private ModelAndView withPaidAppChargeRequestReportOrError(PwaAppProcessingContext processingContext,
                                                             Function<ApplicationChargeRequestReport, ModelAndView> modelAndViewSupplier) {
    return applicationChargeRequestService.getLatestRequestAsApplicationChargeRequestReport(processingContext.getPwaApplication())
        .filter(applicationChargeRequestReport ->
            applicationChargeRequestReport.getPwaAppChargeRequestStatus().equals(PwaAppChargeRequestStatus.PAID))
        .map(modelAndViewSupplier::apply)
        .orElseThrow(() -> new ApplicationChargeException(
                "Could not locate paid application charge request for app Id:" + processingContext.getMasterPwaApplicationId()
            )
        );
  }

}
