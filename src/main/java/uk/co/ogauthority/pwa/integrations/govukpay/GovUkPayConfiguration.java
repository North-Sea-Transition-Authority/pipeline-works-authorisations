package uk.co.ogauthority.pwa.integrations.govukpay;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
final class GovUkPayConfiguration {

  private static final String PAYMENTS_URL = "/v1/payments";

  private static final String REQUEST_METADATA_CONTEXT_KEY = "SOURCE_CONTEXT_PATH";

  private final String apiKey;

  private final String govUkPayBaseUrl;

  private final String govukPayAuthorizationHeaderValue;

  private final RestTemplate restTemplate;

  private final Map<String, Object> defaultRequestMetadata;

  @Autowired
  GovUkPayConfiguration(RestTemplateBuilder restTemplateBuilder,
                        ClientHttpRequestFactory clientHttpRequestFactory,
                        @Value("${govukpay.apiKey}") String apiKey,
                        @Value("${govukpay.api.base-url}") String govUkPayBaseUrl,
                        @Value("${govukpay.connect-timeout-seconds:#{10}}") Long apiConnectionTimeoutSeconds,
                        @Value("${govukpay.read-timeout-seconds:#{10}}") Long apiReadTimeoutSeconds,
                        @Value("${govukpay.request.metadata.app-context-path}") String requestSourceContextPath) {
    this.apiKey = apiKey;
    this.govUkPayBaseUrl = govUkPayBaseUrl;
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;

    this.defaultRequestMetadata = new HashMap<>();
    this.defaultRequestMetadata.put(REQUEST_METADATA_CONTEXT_KEY, requestSourceContextPath);

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

  public Map<String, Object> getDefaultRequestMetadata() {
    return Collections.unmodifiableMap(defaultRequestMetadata);
  }
}
