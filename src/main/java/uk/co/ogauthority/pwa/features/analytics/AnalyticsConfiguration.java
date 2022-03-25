package uk.co.ogauthority.pwa.features.analytics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalyticsConfiguration {

  private final AnalyticsProperties properties;

  private final AnalyticsConfig config;

  @Autowired
  public AnalyticsConfiguration(AnalyticsProperties properties,
                                AnalyticsConfig config) {
    this.properties = properties;
    this.config = config;
  }

  public AnalyticsProperties getProperties() {
    return properties;
  }

  public AnalyticsConfig getConfig() {
    return config;
  }

}
