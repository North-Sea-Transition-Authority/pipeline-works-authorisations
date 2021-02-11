package uk.co.ogauthority.pwa.govukpay;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
class GovUkPayConfiguration {

  private static final String PAYMENTS_URL = "/v1/payments";

  private final String applicationBaseUrl;

  private final String apiKey;

  private final String govUkPayBaseUrl;

  private final String govukPayAuthorizationHeaderValue;

  private final Long apiConnectionTimeoutSeconds;
  private final Long apiReadTimeoutSeconds;

  private final RestTemplateBuilder restTemplateBuilder;
  private final ClientHttpRequestFactory clientHttpRequestFactory;

  private final RestTemplate restTemplate;

  @Autowired
  GovUkPayConfiguration(RestTemplateBuilder restTemplateBuilder,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        @Value("${pwa.url.base}") String applicationBaseUrl,
                        @Value("${govukpay.apiKey}") String apiKey,
                        @Value("${govukpay.api.base-url}") String govUkPayBaseUrl,
                        @Value("${govukpay.connect-timeout-seconds:#{10}}") Long apiConnectionTimeoutSeconds,
                        @Value("${govukpay.read-timeout-seconds:#{10}}") Long apiReadTimeoutSeconds) {
    this.applicationBaseUrl = applicationBaseUrl;
    this.apiKey = apiKey;
    this.govUkPayBaseUrl = govUkPayBaseUrl;
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;
    this.apiConnectionTimeoutSeconds = apiConnectionTimeoutSeconds;
    this.apiReadTimeoutSeconds = apiReadTimeoutSeconds;
    this.restTemplateBuilder = restTemplateBuilder;
    this.clientHttpRequestFactory = clientHttpRequestFactory;

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

  String getApplicationBaseUrl() {
    return applicationBaseUrl;
  }

  String getApiKey() {
    return apiKey;
  }

  String getGovukPayAuthorizationHeaderValue() {
    return govukPayAuthorizationHeaderValue;
  }

  RestTemplate getConfiguredRestTemplate() {
    return restTemplate;
  }
}
