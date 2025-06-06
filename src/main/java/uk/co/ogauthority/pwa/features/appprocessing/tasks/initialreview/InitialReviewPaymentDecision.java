package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.PwaAppChargeRequestStatus;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationInitialReviewResult;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

/**
 * Initial review payment decision.
 */
public enum InitialReviewPaymentDecision {

  PAYMENT_REQUIRED(
      "Payment required",
      PwaApplicationInitialReviewResult.PAYMENT_REQUIRED,
      PwaAppChargeRequestStatus.OPEN,
      PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT
  ),
  PAYMENT_WAIVED(
      "Waive payment",
      PwaApplicationInitialReviewResult.PAYMENT_WAIVED,
      PwaAppChargeRequestStatus.WAIVED,
      PwaApplicationStatus.CASE_OFFICER_REVIEW);


  private final String displayText;
  private final PwaApplicationInitialReviewResult pwaApplicationInitialReviewResult;
  private final PwaAppChargeRequestStatus pwaAppChargeRequestStatus;
  private final PwaApplicationStatus postReviewPwaApplicationStatus;

  InitialReviewPaymentDecision(String displayText,
                               PwaApplicationInitialReviewResult pwaApplicationInitialReviewResult,
                               PwaAppChargeRequestStatus pwaAppChargeRequestStatus,
                               PwaApplicationStatus postReviewPwaApplicationStatus) {
    this.displayText = displayText;
    this.pwaApplicationInitialReviewResult = pwaApplicationInitialReviewResult;
    this.pwaAppChargeRequestStatus = pwaAppChargeRequestStatus;
    this.postReviewPwaApplicationStatus = postReviewPwaApplicationStatus;
  }

  public String getDisplayText() {
    return displayText;
  }

  public PwaApplicationInitialReviewResult getPwaApplicationInitialReviewResult() {
    return pwaApplicationInitialReviewResult;
  }

  public PwaAppChargeRequestStatus getPwaAppChargeRequestStatus() {
    return pwaAppChargeRequestStatus;
  }

  public PwaApplicationStatus getPostReviewPwaApplicationStatus() {
    return postReviewPwaApplicationStatus;
  }
}
