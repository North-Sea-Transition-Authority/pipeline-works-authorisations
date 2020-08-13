package uk.co.ogauthority.pwa.service.enums.pwaapplications;

/**
 * Enumerates all the states that a PWA Consultation can be in.
 */
public enum ConsultationRequestStatus {

  ALLOCATION("Allocation"),
  AWAITING_RESPONSE("Awaiting response"),
  RESPONDED("Response received");

  private final String displayName;

  ConsultationRequestStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}