package uk.co.ogauthority.pwa.features.analytics;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "analytics.config")
@ConstructorBinding
@Validated
public class AnalyticsConfig {

  @NotNull
  private final boolean enabled;

  private final String apiSecret;

  private final String endpointUrl;

  private final String userAgent;

  private final Integer connectionTimeoutSeconds;

  public AnalyticsConfig(boolean enabled,
                         String apiSecret,
                         String endpointUrl,
                         String userAgent,
                         Integer connectionTimeoutSeconds) {
    this.enabled = enabled;
    this.apiSecret = apiSecret;
    this.endpointUrl = endpointUrl;
    this.userAgent = userAgent;
    this.connectionTimeoutSeconds = connectionTimeoutSeconds;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getApiSecret() {
    return apiSecret;
  }

  public String getEndpointUrl() {
    return endpointUrl;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public Integer getConnectionTimeoutSeconds() {
    return connectionTimeoutSeconds;
  }

}
