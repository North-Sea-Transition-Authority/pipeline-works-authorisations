package uk.co.ogauthority.pwa.service.appprocessing.prepareconsent;

import java.util.List;
import uk.co.ogauthority.pwa.service.appprocessing.appprocessingwarning.NonBlockingTasksWarning;

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
