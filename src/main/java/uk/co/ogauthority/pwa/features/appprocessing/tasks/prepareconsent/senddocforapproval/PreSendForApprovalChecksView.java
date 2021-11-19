package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

import java.util.List;
import uk.co.ogauthority.pwa.features.appprocessing.processingwarnings.NonBlockingTasksWarning;

/**
 * Materialise reasons for case officers to block sending for approval.
 */
public final class PreSendForApprovalChecksView {

  private final List<FailedSendForApprovalCheck> failedSendForApprovalChecks;

  private final List<ParallelConsentView> parallelConsentViews;

  private final NonBlockingTasksWarning nonBlockingTasksWarning;

  PreSendForApprovalChecksView(List<FailedSendForApprovalCheck> failedSendForApprovalChecks,
                               List<ParallelConsentView> parallelConsentViews,
                               NonBlockingTasksWarning nonBlockingTasksWarning) {
    this.failedSendForApprovalChecks = failedSendForApprovalChecks;
    this.parallelConsentViews = parallelConsentViews;
    this.nonBlockingTasksWarning = nonBlockingTasksWarning;
  }

  public List<FailedSendForApprovalCheck> getFailedSendForApprovalChecks() {
    return failedSendForApprovalChecks;
  }

  public List<ParallelConsentView> getParallelConsentViews() {
    return parallelConsentViews;
  }

  public NonBlockingTasksWarning getNonBlockingTasksWarning() {
    return nonBlockingTasksWarning;
  }

}
