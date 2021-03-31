package uk.co.ogauthority.pwa.govukpay;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
final class GovUkPayConfiguration {

  private static final String PAYMENTS_URL = "/v1/payments";

  private final String apiKey;

  private final String govUkPayBaseUrl;

  private final String govukPayAuthorizationHeaderValue;

  private final RestTemplate restTemplate;

  @Autowired
  GovUkPayConfiguration(RestTemplateBuilder restTemplateBuilder,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        @Value("${govukpay.apiKey}") String apiKey,
                        @Value("${govukpay.api.base-url}") String govUkPayBaseUrl,
                        @Value("${govukpay.connect-timeout-seconds:#{10}}") Long apiConnectionTimeoutSeconds,
                        @Value("${govukpay.read-timeout-seconds:#{10}}") Long apiReadTimeoutSeconds) {
    this.apiKey = apiKey;
    this.govUkPayBaseUrl = govUkPayBaseUrl;
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;

    this.restTemplate = restTemplateBuilder
        .setConnectTimeout(Duration.ofSeconds(apiConnectionTimeoutSeconds))
        .setReadTimeout(Duration.ofSeconds(apiReadTimeoutSeconds))
        .defaultHeader("User-Agent", "fivium-gov-uk-payment-client")
        .requestFactory(() -> clientHttpRequestFactory)
        .build();

  }

  String getGovukPayBaseUrl() {
    return this.govUkPayBaseUrl;
  }

  String getPaymentsEndpoint() {
    return PAYMENTS_URL;
  }

  String getGovukPayAuthorizationHeaderValue() {
    return govukPayAuthorizationHeaderValue;
  }

  RestTemplate getConfiguredRestTemplate() {
    return restTemplate;
  }

  public String getApiKey() {
    return apiKey;
  }
}
