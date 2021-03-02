package uk.co.ogauthority.pwa.controller.appprocessing.processingcharges;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ProcessPaymentAttemptOutcome;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;

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
      // TODO PWA-1158 - confirmation page/ charge request status page.
      FlashUtils.success(redirectAttributes,
          String.format("Payment for application %s complete", pwaApplication.getAppReference()));
      return CaseManagementUtils.redirectCaseManagement(pwaApplication);
    } else {
      FlashUtils.info(redirectAttributes,
          String.format("No payment for application %s has been completed", pwaApplication.getAppReference()));
      return CaseManagementUtils.redirectCaseManagement(pwaApplication);
    }

  }

}
