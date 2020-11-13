package uk.co.ogauthority.pwa.service.enums.pwaapplications;

/**
 * Enumerates all the states that a PWA Consultation can be in.
 */
public enum ConsultationRequestStatus {

  ALLOCATION("Allocation", true),
  AWAITING_RESPONSE("Awaiting response", true),
  RESPONDED("Response received", false),
  WITHDRAWN("Withdrawn", false);

  private final String displayName;
  private final boolean requestOpen;

  ConsultationRequestStatus(String displayName, boolean requestOpen) {
    this.displayName = displayName;
    this.requestOpen = requestOpen;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isRequestOpen() {
    return requestOpen;
  }
}