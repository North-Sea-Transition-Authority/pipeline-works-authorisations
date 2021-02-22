package uk.co.ogauthority.pwa.service.enums.pwaapplications;

/**
 * Enumerates all the states that a PWA application can be in.
 * If you are adding a status you will likely need to add it to the {@link ApplicationState} list.
 */
public enum PwaApplicationStatus {

  DRAFT("Draft"),
  UPDATE_REQUESTED("Draft"),
  INITIAL_SUBMISSION_REVIEW("Submitted - awaiting review"),
  AWAITING_APPLICATION_PAYMENT("Submitted - awaiting payment"),
  CASE_OFFICER_REVIEW("Case officer review"),
  WITHDRAWN("Withdrawn"),
  DELETED("Deleted"),
  COMPLETE("Complete");

  private final String displayName;

  PwaApplicationStatus(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
