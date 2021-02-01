package uk.co.ogauthority.pwa.pay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

  @Autowired
  GovUkPayConfiguration(@Value("${pwa.url.base}") String applicationBaseUrl,
                        @Value("${context-path}") String contextPath,
                        @Value("${govukpay.apiKey}") String apiKey,
                        @Value("${govukpay.log.level:#{null}}") String logLevel) {
    this.applicationBaseUrl = applicationBaseUrl;
    this.contextPath = contextPath;
    this.apiKey = apiKey;
    this.govukPayAuthorizationHeaderValue = "Bearer " + apiKey;
    this.logLevel = logLevel;
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
}
