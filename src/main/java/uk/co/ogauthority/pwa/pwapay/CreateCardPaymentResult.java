package uk.co.ogauthority.pwa.pwapay;

import java.util.Optional;

/**
 * Captures the created PwaPaymentRequest and url to start external journey.
 */
public final class CreateCardPaymentResult {

  private final PwaPaymentRequest paymentRequest;
  private final String startExternalJourneyUrl;

  CreateCardPaymentResult(PwaPaymentRequest paymentRequest,
                          String startExternalJourneyUrl) {
    this.paymentRequest = paymentRequest;
    this.startExternalJourneyUrl = startExternalJourneyUrl;
  }

  public PwaPaymentRequest getPwaPaymentRequest() {
    return paymentRequest;
  }

  public PaymentRequestStatus getPaymentRequestStatus() {
    return paymentRequest.getRequestStatus();
  }

  public Optional<String> getStartExternalJourneyUrl() {
    return Optional.ofNullable(startExternalJourneyUrl);
  }
}
