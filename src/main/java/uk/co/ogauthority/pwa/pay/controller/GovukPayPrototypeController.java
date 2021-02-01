package uk.co.ogauthority.pwa.pay.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.pay.GovUkPayClient;
import uk.co.ogauthority.pwa.pay.NewCardPaymentRequest;

@Controller
@RequestMapping("/govukpay")
@SessionAttributes("paymentJourneySessionStore")
public class GovukPayPrototypeController {

  // TODO PWA-1113  needs to sit in app domain payments service
  @Value("${pwa.url.base}")
  private String pwaUrlBase;

  @Value("${context-path}")
  private String contextPath;

  private final GovUkPayClient govUkPayClient;

  @Autowired
  public GovukPayPrototypeController(GovUkPayClient govUkPayClient) {
    this.govUkPayClient = govUkPayClient;
  }

  @Bean
  @SessionScope
  public PaymentJourneySessionStore paymentJourneySessionStore() {
    return new PaymentJourneySessionStore();
  }

  @Resource(name = "paymentJourneySessionStore")
  private PaymentJourneySessionStore paymentJourneySessionStore;

  private String getReturnUrlForActiveUuid() {
    return pwaUrlBase + contextPath + ReverseRouter.route(
        on(this.getClass()).handleReturnUrl(paymentJourneySessionStore.getActiveUuid(), null));
  }

  @GetMapping("/start")
  public ModelAndView renderStartPaymentJourney(AuthenticatedUserAccount user) {
    var modelAndView = new ModelAndView("testTemplates/govukpay_start")
        .addObject("journeyUuid", paymentJourneySessionStore.getActiveUuid().toString())
        .addObject("returnUrl", getReturnUrlForActiveUuid())
        .addObject("uuids", paymentJourneySessionStore.getHistoricalUuidSet());

    return modelAndView;
  }


  @PostMapping("/start")
  public RedirectView startPaymentJourney(AuthenticatedUserAccount user) {

    var newCardPaymentRequest = new NewCardPaymentRequest(
        100, // pence
        "My Reference:" + paymentJourneySessionStore.getActiveUuid().toString(),
        "This is a description of the payment",
        getReturnUrlForActiveUuid()
    );

    var paymentResult = govUkPayClient.createCardPaymentJourney(newCardPaymentRequest);

    paymentJourneySessionStore.startJourney(paymentResult.getPaymentId());

    return new RedirectView(paymentResult.getStartExternalPaymentJourneyUrl());

  }


  @GetMapping("/return/{paymentJourneyUuid}")
  public ModelAndView handleReturnUrl(@PathVariable("paymentJourneyUuid") UUID paymentJourneyUuid,
                                      AuthenticatedUserAccount user) {
    paymentJourneySessionStore.setHistoricalUuidAsActive(paymentJourneyUuid);

    var modelAndView = new ModelAndView("testTemplates/govukpay_end")
        .addObject("journeyUuid", paymentJourneySessionStore.getActiveUuid());

    return modelAndView;
  }

}


