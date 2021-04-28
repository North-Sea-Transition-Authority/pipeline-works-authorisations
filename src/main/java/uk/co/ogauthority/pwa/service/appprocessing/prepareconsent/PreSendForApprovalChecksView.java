package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.util.List;

/**
 * Materialise reasons for case officers to block sending for approval
 */
public final class PreSendForApprovalChecksView {

  private final List<FailedSendForApprovalCheck> failedSendForApprovalChecks;

  private final List<ParallelConsentView> parallelConsentViews;

  PreSendForApprovalChecksView(List<FailedSendForApprovalCheck> failedSendForApprovalChecks,
                               List<ParallelConsentView> parallelConsentViews) {
    this.failedSendForApprovalChecks = failedSendForApprovalChecks;
    this.parallelConsentViews = parallelConsentViews;
  }

  public List<FailedSendForApprovalCheck> getFailedSendForApprovalChecks() {
    return failedSendForApprovalChecks;
  }

  public List<ParallelConsentView> getParallelConsentViews() {
    return parallelConsentViews;
  }
}
