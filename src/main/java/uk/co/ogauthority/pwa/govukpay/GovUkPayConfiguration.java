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

  private static final String DEBUG_LOG_LEVEL = "DEBUG";

  private static final String GOVUK_PAY_BASE = "https://publicapi.payments.service.gov.uk";
  private static final String PAYMENTS_URL = "/v1/payments";

  private final String applicationBaseUrl;

  private final String contextPath;

  private final String apiKey;

  private final String logLevel;

  private final String govukPayAuthorizationHeaderValue;

  private final Long apiConnectionTimeoutSeconds;
  private final Long apiReadTimeoutSeconds;

  private final RestTemplateBuilder restTemplateBuilder;
  private final ClientHttpRequestFactory clientHttpRequestFactory;

  @Autowired
  GovUkPayConfiguration(RestTemplateBuilder restTemplateBuilder,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        @Value("${pwa.url.base}") String applicationBaseUrl,
                        @Value("${context-path}") String contextPath,
                        @Value("${govukpay.apiKey}") String apiKey,
                        @Value("${govukpay.connect-timeout-seconds:#{10}}") Long apiConnectionTimeoutSeconds,
                        @Value("${govukpay.read-timeout-seconds:#{10}}") Long apiReadTimeoutSeconds,
                        @Value("${govukpay.log.level:#{null}}") String logLevel) {
    this.applicationBaseUrl = applicationBaseUrl;
    this.contextPath = contextPath;
    this.apiKey = apiKey;
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;
    this.apiConnectionTimeoutSeconds = apiConnectionTimeoutSeconds;
    this.apiReadTimeoutSeconds =apiReadTimeoutSeconds;
    this.logLevel = logLevel;
    this.restTemplateBuilder = restTemplateBuilder;
    this.clientHttpRequestFactory = clientHttpRequestFactory;

  }

  boolean isDebugEnabled() {
    return DEBUG_LOG_LEVEL.equals(this.logLevel);
  }

  String getGovukPayBaseUrl() {
    return GOVUK_PAY_BASE;
  }

  String getPaymentsEndpoint() {
    return PAYMENTS_URL;
  }

  String getApplicationBaseUrl() {
    return applicationBaseUrl;
  }

  String getContextPath() {
    return contextPath;
  }

  String getApiKey() {
    return apiKey;
  }

  String getGovukPayAuthorizationHeaderValue() {
    return govukPayAuthorizationHeaderValue;
  }

  RestTemplate getConfiguredRestTemplate(){
    return restTemplateBuilder
        .setConnectTimeout(Duration.ofSeconds(apiConnectionTimeoutSeconds))
        .setReadTimeout(Duration.ofSeconds(apiReadTimeoutSeconds))
        .defaultHeader("User-Agent", "fivium-gov-uk-payment-client")
        .requestFactory(() -> clientHttpRequestFactory)
        .build();
  }
}
