package uk.co.ogauthority.pwa.features.analytics;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "analytics.properties")
@ConstructorBinding
@Validated
public class AnalyticsProperties {

  private final String appTag;

  private final String globalTag;

  public AnalyticsProperties(String appTag,
                             String globalTag) {
    this.appTag = appTag;
    this.globalTag = globalTag;
  }

  public String getAppTag() {
    return appTag;
  }

  public String getGlobalTag() {
    return globalTag;
  }

}