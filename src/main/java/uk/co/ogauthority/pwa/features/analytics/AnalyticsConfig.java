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

  private final String appTagApiSecret;

  private final String globalTagApiSecret;

  private final String endpointUrl;

  private final String userAgent;

  private final Integer connectionTimeoutSeconds;

  public AnalyticsConfig(boolean enabled,
                         String appTagApiSecret,
                         String globalTagApiSecret,
                         String endpointUrl,
                         String userAgent,
                         Integer connectionTimeoutSeconds) {
    this.enabled = enabled;
    this.appTagApiSecret = appTagApiSecret;
    this.globalTagApiSecret = globalTagApiSecret;
    this.endpointUrl = endpointUrl;
    this.userAgent = userAgent;
    this.connectionTimeoutSeconds = connectionTimeoutSeconds;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getAppTagApiSecret() {
    return appTagApiSecret;
  }

  public String getGlobalTagApiSecret() {
    return globalTagApiSecret;
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
