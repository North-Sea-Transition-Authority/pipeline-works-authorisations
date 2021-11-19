package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import java.util.Objects;

/**
 * Captures reason and displayable text when preventing a consent document being sent for approval.
 */
public final class FailedSendForApprovalCheck {
  private final SendConsentForApprovalRequirement sendConsentForApprovalRequirement;
  private final String reason;

  FailedSendForApprovalCheck(SendConsentForApprovalRequirement sendConsentForApprovalRequirement) {
    this.sendConsentForApprovalRequirement = sendConsentForApprovalRequirement;
    this.reason = sendConsentForApprovalRequirement.getDefaultFailureReason();
  }

  public SendConsentForApprovalRequirement getSendConsentForApprovalRequirement() {
    return sendConsentForApprovalRequirement;
  }

  public String getReason() {
    return reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FailedSendForApprovalCheck that = (FailedSendForApprovalCheck) o;
    return sendConsentForApprovalRequirement == that.sendConsentForApprovalRequirement
        && Objects.equals(reason, that.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sendConsentForApprovalRequirement, reason);
  }

  @Override
  public String toString() {
    return "FailedSendforApprovalCheck{" +
        "sendConsentForApprovalRequirement=" + sendConsentForApprovalRequirement +
        ", reason='" + reason + '\'' +
        '}';
  }
}
