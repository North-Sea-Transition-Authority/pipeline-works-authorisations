package uk.co.ogauthority.pwa.pwapay;

public enum PaymentRequestStatus {

  PENDING(JourneyState.IN_PROGRESS),
  IN_PROGRESS(JourneyState.IN_PROGRESS),
  FAILED_TO_CREATE(JourneyState.FINISHED),
  CANCELLED(JourneyState.FINISHED),
  PAYMENT_COMPLETE(JourneyState.FINISHED),
  COMPLETE_WITHOUT_PAYMENT(JourneyState.FINISHED);

  private final JourneyState journeyState;

  PaymentRequestStatus(JourneyState journeyState) {
    this.journeyState = journeyState;
  }

  public JourneyState getJourneyState() {
    return journeyState;
  }

  enum JourneyState {
    IN_PROGRESS, FINISHED
  }
}
