package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeException;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestReport;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ProcessPaymentAttemptOutcome;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
public class IndustryPaymentCallbackController {

  private final ApplicationChargeRequestService applicationChargeRequestService;

  @Autowired
  public IndustryPaymentCallbackController(ApplicationChargeRequestService applicationChargeRequestService) {
    this.applicationChargeRequestService = applicationChargeRequestService;
  }

  @GetMapping("/pwa-application/pay/return/{paymentJourneyUuid}")
  public ModelAndView reconcilePaymentRequestAndRedirect(@PathVariable("paymentJourneyUuid") UUID paymentJourneyUuid,
                                                         AuthenticatedUserAccount user,
                                                         RedirectAttributes redirectAttributes) {
    var reconciledPaymentAttempt = applicationChargeRequestService
        .reconcilePaymentRequestCallbackUuidToPaymentAttempt(paymentJourneyUuid);

    var pwaApplication = reconciledPaymentAttempt.getPwaAppChargeRequest().getPwaApplication();

    var processPaymentAttemptOutcome = applicationChargeRequestService.processPaymentAttempt(
        reconciledPaymentAttempt,
        user
    );

    if (processPaymentAttemptOutcome.equals(ProcessPaymentAttemptOutcome.CHARGE_REQUEST_PAID)) {
      return ReverseRouter.redirect(on(this.getClass())
          .renderPaymentResult(pwaApplication.getId(), pwaApplication.getApplicationType(), null));
    } else {
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
