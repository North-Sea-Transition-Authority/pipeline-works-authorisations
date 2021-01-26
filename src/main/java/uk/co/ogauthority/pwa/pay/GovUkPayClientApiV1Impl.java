package uk.co.ogauthority.pwa.pay;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.co.ogauthority.pwa.pay.api.model.cardpayment.request.CreateCardPaymentRequest;
import uk.co.ogauthority.pwa.pay.api.model.cardpayment.response.CreatePaymentResult;

@Service
class GovUkPayClientApiV1Impl implements GovUkPayClient {

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

    RestTemplate restTemplate = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, govUkPayConfiguration.getGovukPayAuthorizationHeaderValue());
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<CreateCardPaymentRequest> request = new HttpEntity<>(paymentRequest, headers);
    ResponseEntity<CreatePaymentResult> response = restTemplate
        .exchange(getPaymentsUrl(), HttpMethod.POST, request, CreatePaymentResult.class);

    return apiV1RequestDataMapper.mapNewCardPaymentResponse(response.getBody());

  }

  private String getPaymentsUrl() {
    return govUkPayConfiguration.getGovukPayBaseUrl() + govUkPayConfiguration.getPaymentsEndpoint();
  }
}
