package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges;

/**
 * When processing a charge request payment attempt, describe the end result of the processing.
 */
public enum ProcessPaymentAttemptOutcome {
  /**
   * Charge request is now paid.
   */
  CHARGE_REQUEST_PAID,
  /**
   * Charge request is unchanged after processing payment attempt.
   */
  CHARGE_REQUEST_UNCHANGED;

}
