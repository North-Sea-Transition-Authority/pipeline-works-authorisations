package uk.co.ogauthority.pwa.support.paydashboard;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.pwapay.PwaPaymentRequest;
import uk.co.ogauthority.pwa.pwapay.PwaPaymentService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;

@Controller
@Profile("development")
@RequestMapping("/support/payments")
public class PaymentRequestDashboardController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestDashboardController.class);

  private final PwaPaymentService pwaPaymentService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PaymentRequestDashboardController(PwaPaymentService pwaPaymentService,
                                           ControllerHelperService controllerHelperService) {
    this.pwaPaymentService = pwaPaymentService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView dashboard(AuthenticatedUserAccount user,
                                @RequestParam(name = "page", defaultValue = "0") Integer page,
                                @ModelAttribute("form") TestPaymentForm testPaymentForm) {
    return dashboardModelAndView(page);
  }

  private String dashboardPageUrl(int page) {
    return ReverseRouter.route(on(PaymentRequestDashboardController.class).dashboard(null, page, null));
  }

  private PaymentRequestView pageReguestViewMapper(PwaPaymentRequest paymentRequest) {
    return new PaymentRequestView(
        paymentRequest,
        ReverseRouter.route(on(PaymentRequestDashboardController.class).viewStoredPaymentRequestJourney(
            null,
            paymentRequest.getUuid()
        ))
    );
  }

  private ModelAndView dashboardModelAndView(int page) {
    var paymentsList = PageView.fromPage(
        pwaPaymentService.getPwaPaymentRequests(page),
        this::dashboardPageUrl,
        this::pageReguestViewMapper
    );

    return new ModelAndView("support/payments/paymentsDashboard")
        .addObject("payments", paymentsList)
        .addObject("startTestPaymentUrl", ReverseRouter.route(on(PaymentRequestDashboardController.class)
            .startPaymentJourney(null, null, null))
        );

  }

  @GetMapping("/{uuid}/view")
  public ModelAndView viewStoredPaymentRequestJourney(AuthenticatedUserAccount user,
                                                      @PathVariable("uuid") UUID uuid) {
    var paymentRequest = pwaPaymentService.getGovUkPaymentRequestOrError(uuid);
    return new ModelAndView("support/payments/viewPayment")
        .addObject("pwaPaymentRequest", paymentRequest)
        .addObject("paymentActionUrl",
            ReverseRouter.route(on(PaymentRequestDashboardController.class).updatePayment(null, uuid, null)))
        .addObject("paymentsDashboardUrl",
            ReverseRouter.route(on(PaymentRequestDashboardController.class).dashboard(null, null, null)));
  }

  @PostMapping("{uuid}/update")
  public ModelAndView updatePayment(AuthenticatedUserAccount user,
                                    @PathVariable("uuid") UUID uuid,
                                    @ModelAttribute("action") PwaPaymentSupportAction action) {

    var paymentRequest = pwaPaymentService.getGovUkPaymentRequestOrError(uuid);

    switch (action) {
      case CANCEL:
        pwaPaymentService.cancelPayment(paymentRequest);
        break;
      case REFRESH:
        pwaPaymentService.refreshPwaPaymentRequestData(paymentRequest);
        break;
      default: LOGGER.warn("Unrecognised updated payment action: " + action);
    }

    return ReverseRouter.redirect(
        on(PaymentRequestDashboardController.class).viewStoredPaymentRequestJourney(null, uuid));

  }


  @PostMapping("/start")
  public ModelAndView startPaymentJourney(AuthenticatedUserAccount user,
                                          @Valid @ModelAttribute("form") TestPaymentForm testPaymentForm,
                                          BindingResult bindingResult) {

    return controllerHelperService.checkErrorsAndRedirect(
        bindingResult,
        dashboardModelAndView(0),
        () -> new ModelAndView("redirect:" +
            pwaPaymentService.createCardPayment(
                testPaymentForm.getAmount(),
                testPaymentForm.getReference(),
                "Support Test Payment",
                uuid -> ReverseRouter.route(on(TestPaymentCallbackController.class).handleReturnUrl(uuid, null))
            )
        )
    );

  }

}


