package uk.co.ogauthority.pwa.service.appprocessing.options;

/**
 * Enum to summarise the possible Options Approval states that can exist within the application.
 */
public enum OptionsApprovalStatus {

  NOT_APPLICABLE(false),
  NOT_APPROVED(false),
  APPROVED_UNRESPONDED(true),
  APPROVED_RESPONDED(true);

  private final boolean optionsApproved;

  OptionsApprovalStatus(boolean optionsApproved) {
    this.optionsApproved = optionsApproved;
  }

  public boolean isOptionsApproved() {
    return optionsApproved;
  }
}
