package uk.co.ogauthority.pwa.model.enums.feedback;

import java.util.Arrays;
import java.util.Map;
import uk.co.ogauthority.pwa.util.StreamUtils;

public enum ServiceFeedbackRating {

  VERY_SATISFIED("Very satisfied"),
  SATISFIED("Satisfied"),
  NEITHER("Neither satisfied or dissatisfied"),
  DISSATISFIED("Dissatisfied"),
  VERY_DISSATISFIED("Very dissatisfied");

  private final String displayName;

  ServiceFeedbackRating(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static Map<String, String> getAllAsMap() {
    return Arrays.stream(values())
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ServiceFeedbackRating::getDisplayName));
  }
}
