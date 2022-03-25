package uk.co.ogauthority.pwa.features.analytics;

import java.util.HashMap;
import java.util.Map;

class AnalyticsEvent {

  private final String name;

  private final Map<String, String> params;

  AnalyticsEvent(Builder builder) {
    this.name = builder.name;
    this.params = builder.params;
  }

  public String getName() {
    return name;
  }

  public Map<String, String> getParams() {
    return params;
  }

  static class Builder {

    private final String name;

    private final Map<String, String> params;

    public Builder(String name) {
      this.name = name;
      this.params = new HashMap<>();
    }

    public AnalyticsEvent.Builder withParam(String key, String value) {
      this.params.put(key, value);
      return this;
    }

    public AnalyticsEvent build() {
      return new AnalyticsEvent(this);
    }

  }

}