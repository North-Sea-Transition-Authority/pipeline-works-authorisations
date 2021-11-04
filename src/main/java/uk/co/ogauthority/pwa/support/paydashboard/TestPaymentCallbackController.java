package uk.co.ogauthority.pwa.support.paydashboard;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Controller
@Profile("development")
@RequestMapping("/govukpay/test-callback")
public class TestPaymentCallbackController {

  private final PwaPaymentService pwaPaymentService;

  @Autowired
  public TestPaymentCallbackController(PwaPaymentService pwaPaymentService) {
    this.pwaPaymentService = pwaPaymentService;
  }

  @GetMapping("/return/{paymentJourneyUuid}")
  public ModelAndView handleReturnUrl(@PathVariable("paymentJourneyUuid") UUID paymentJourneyUuid,
                                      AuthenticatedUserAccount user) {
    var paymentRequest = pwaPaymentService.getGovUkPaymentRequestOrError(paymentJourneyUuid);
    pwaPaymentService.refreshPwaPaymentRequestData(paymentRequest);
    return ReverseRouter.redirect(on(PaymentRequestDashboardController.class).viewStoredPaymentRequestJourney(null, paymentJourneyUuid));
  }

}


