package uk.co.ogauthority.pwa.features.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

class AnalyticsPayload {

  @JsonProperty("client_id")
  private final String clientId;

  private final List<AnalyticsEvent> events;

  public AnalyticsPayload(String clientId, List<AnalyticsEvent> events) {
    this.clientId = clientId;
    this.events = events;
  }

  public String getClientId() {
    return clientId;
  }

  public List<AnalyticsEvent> getEvents() {
    return events;
  }

}
