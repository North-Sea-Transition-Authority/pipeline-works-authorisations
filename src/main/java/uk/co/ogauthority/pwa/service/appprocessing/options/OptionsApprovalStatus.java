package uk.co.ogauthority.pwa.service.appprocessing.options;

/**
 * Enum to summarise the possible Options Approval states that can exist within the application.
 */
public enum OptionsApprovalStatus {

  NOT_APPLICABLE(false, false),
  NOT_APPROVED(false, false),
  APPROVED_UNRESPONDED(true, false),
  APPROVED_OTHER_CONFIRMED(true, false),
  APPROVED_CONSENTED_OPTION_CONFIRMED(true, true);

  private final boolean optionsApproved;

  private final boolean consentedOptionConfirmed;

  OptionsApprovalStatus(boolean optionsApproved, boolean consentedOptionConfirmed) {
    this.optionsApproved = optionsApproved;
    this.consentedOptionConfirmed = consentedOptionConfirmed;
  }

  public boolean isOptionsApproved() {
    return optionsApproved;
  }

  public boolean isConsentedOptionConfirmed() {
    return consentedOptionConfirmed;
  }
}
