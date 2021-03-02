package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

/**
 * Captures data required to decide how to direct users when trying to start a payment attempt.
 */
public class CreatePaymentAttemptResult {

  private final String startExternalJourneyUrl;
  private final AttemptOutcome attemptOutcome;

  CreatePaymentAttemptResult(String startExternalJourneyUrl,
                             AttemptOutcome attemptOutcome) {
    this.startExternalJourneyUrl = startExternalJourneyUrl;
    this.attemptOutcome = attemptOutcome;
  }

  public String getStartExternalJourneyUrl() {
    return startExternalJourneyUrl;
  }

  public AttemptOutcome getPaymentAttemptOutcome() {
    return attemptOutcome;
  }

  public enum AttemptOutcome {
    PAYMENT_CREATED,
    COMPLETED_PAYMENT_EXISTS
  }
}
