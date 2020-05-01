package uk.co.ogauthority.pwa.service.enums.pwaapplications;

/**
 * Enumerates all the states that a PWA application can be in.
 */
public enum PwaApplicationStatus {

  DRAFT("Draft"), INITIAL_SUBMISSION_REVIEW("Submitted - awaiting review");

  private final String displayName;

  PwaApplicationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
