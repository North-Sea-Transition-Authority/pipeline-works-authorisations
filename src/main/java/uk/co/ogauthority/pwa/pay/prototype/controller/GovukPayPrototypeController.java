package uk.co.ogauthority.pwa.pay.prototype.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.request.CreateCardPaymentRequest;
import uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.response.CreatePaymentResult;

@Controller
@RequestMapping("/govukpay")
@SessionAttributes("paymentJourneySessionStore")
public class GovukPayPrototypeController {

  private static final Logger LOGGER = LoggerFactory.getLogger(GovukPayPrototypeController.class);

  private static final String GOVUK_PAY_BASE = "https://publicapi.payments.service.gov.uk";
  private static final String CREATE_PAYMENT = "/v1/payments";


  @Value("${pwa.url.base}")
  private String pwaUrlBase;

  @Value("${context-path}")
  private String contextPath;

  private final String govukPayAuthorizationHeaderValue;

  @Autowired
  public GovukPayPrototypeController(@Value("${govukpay.apiKey}") String apiKey) {
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;
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

    CreateCardPaymentRequest requestBodyParams = new CreateCardPaymentRequest(
        100, // pence
        "My Reference:" + paymentJourneySessionStore.getActiveUuid().toString(),
        "This is a description of the payment",
        getReturnUrlForActiveUuid()
    );


    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, govukPayAuthorizationHeaderValue);
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);


    HttpEntity<CreateCardPaymentRequest> request = new HttpEntity<>(requestBodyParams, headers);
    ResponseEntity<CreatePaymentResult> response = restTemplate
        .exchange(GOVUK_PAY_BASE + CREATE_PAYMENT, HttpMethod.POST, request, CreatePaymentResult.class);

    paymentJourneySessionStore.startJourney(response.getBody().getPaymentId());

    return new RedirectView(response.getBody().getLinks().getNextUrl().getHref());

  }


  @GetMapping("/return/{paymentJourneyUuid}")
  public ModelAndView handleReturnUrl(@PathVariable("paymentJourneyUuid") UUID paymentJourneyUuid,
                                      AuthenticatedUserAccount user) {
    paymentJourneySessionStore.setHistoricalUUIDAsActive(paymentJourneyUuid);

    var modelAndView = new ModelAndView("testTemplates/govukpay_end")
        .addObject("journeyUuid", paymentJourneySessionStore.getActiveUuid());

    return modelAndView;
  }

}


