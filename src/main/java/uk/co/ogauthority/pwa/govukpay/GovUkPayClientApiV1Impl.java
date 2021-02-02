package uk.co.ogauthority.pwa.govukpay;

import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.request.CreateCardPaymentRequest;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.response.CreatePaymentResult;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.response.GetPaymentResult;

@Service
class GovUkPayClientApiV1Impl implements GovUkPayCardPaymentClient {

  private final ApiV1RequestDataMapper apiV1RequestDataMapper;
  private final GovUkPayConfiguration govUkPayConfiguration;

  @Autowired
  GovUkPayClientApiV1Impl(ApiV1RequestDataMapper apiV1RequestDataMapper,
                          GovUkPayConfiguration config) {
    this.apiV1RequestDataMapper = apiV1RequestDataMapper;
    this.govUkPayConfiguration = config;
  }

  @Override
  public NewCardPaymentResult createCardPaymentJourney(NewCardPaymentRequest newCardPaymentRequest) {
    var paymentRequest = apiV1RequestDataMapper.mapNewCardPaymentRequest(newCardPaymentRequest);

    RestTemplate restTemplate = govUkPayConfiguration.getConfiguredRestTemplate();

    HttpHeaders headers = getAuthorisedHttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<CreateCardPaymentRequest> request = new HttpEntity<>(paymentRequest, headers);

    ResponseEntity<CreatePaymentResult> response = restTemplate
        .exchange(getBasePaymentsUrl(), HttpMethod.POST, request, CreatePaymentResult.class);
    return apiV1RequestDataMapper.mapNewCardPaymentResult(response.getBody());

  }

  @Override
  public PaymentJourneyData getCardPaymentJourneyData(String paymentId) {
    RestTemplate restTemplate = govUkPayConfiguration.getConfiguredRestTemplate();

    HttpHeaders headers = getAuthorisedHttpHeaders();

    var entity = new HttpEntity<>(headers);
    var resourceUrl = getSinglePaymentUrl(paymentId);
    ResponseEntity<GetPaymentResult> result = restTemplate.exchange(
        resourceUrl,
        HttpMethod.GET,
        entity,
        GetPaymentResult.class
    );

    if (!result.getStatusCode().equals(HttpStatus.OK)) {
      throw new GovUkPayRequestFailure(
          String.format("Received status code %s from %s", result.getStatusCodeValue(), resourceUrl)
      );
    }
    return apiV1RequestDataMapper.mapGetPaymentResult(Objects.requireNonNull(result.getBody()));


  }

  @Override
  public void cancelCardPaymentJourney(String paymentId) {
    RestTemplate restTemplate = govUkPayConfiguration.getConfiguredRestTemplate();

    HttpHeaders headers = getAuthorisedHttpHeaders();

    var entity = new HttpEntity<>(headers);
    var resourceUrl = getCancelPaymentUrl(paymentId);

    ResponseEntity<String> result = restTemplate.exchange(
        resourceUrl,
        HttpMethod.GET,
        entity,
        String.class
    );

    if (!result.getStatusCode().equals(HttpStatus.OK)) {
      throw new GovUkPayRequestFailure(
          String.format("Received status code %s from %s", result.getStatusCodeValue(), resourceUrl)
      );
    }

  }

  private String getBasePaymentsUrl() {
    return govUkPayConfiguration.getGovukPayBaseUrl() + govUkPayConfiguration.getPaymentsEndpoint();
  }

  private String getSinglePaymentUrl(String paymentId) {
    return String.format("%s/%s", getBasePaymentsUrl(), paymentId);
  }

  private String getCancelPaymentUrl(String paymentId) {
    return String.format("%s/%s/cancel", getBasePaymentsUrl(), paymentId);
  }

  private HttpHeaders getAuthorisedHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, govUkPayConfiguration.getGovukPayAuthorizationHeaderValue());
    return headers;
  }
}
