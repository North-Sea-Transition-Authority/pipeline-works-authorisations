package uk.co.ogauthority.pwa.features.analytics;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsEventForm {

  private AnalyticsEventCategory eventCategory;

  private Map<String, String> paramMap = new HashMap<>();

  public AnalyticsEventCategory getEventCategory() {
    return eventCategory;
  }

  public void setEventCategory(AnalyticsEventCategory eventCategory) {
    this.eventCategory = eventCategory;
  }

  public Map<String, String> getParamMap() {
    return paramMap;
  }

  public void setParamMap(Map<String, String> paramMap) {
    this.paramMap = paramMap;
  }

}