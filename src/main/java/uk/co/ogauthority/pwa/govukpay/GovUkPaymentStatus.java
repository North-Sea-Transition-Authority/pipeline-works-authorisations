package uk.co.ogauthority.pwa.govukpay;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GovUkPaymentStatus {

  /**
   * Copied Gov Pay status documentation (https://docs.payments.service.gov.uk/api_reference/#payment-status-lifecycle):
   * created:    Payment created using the API. Your user has not yet visited next_url.
   * started:    Your user has submitted payment details, and gone through authentication if required.
   *             The PSP has authorised the payment, but the user has not yet selected Confirm.
   * submitted:  Your user has submitted payment details, and gone through authentication if required.
   *             The PSP has authorised the payment, but the user has not yet selected Confirm.
   * capturable: The payment is a delayed capture, and your user has submitted payment details and selected Confirm.
   * success:    Your user successfully completed the payment by selecting Confirm.
   * failed:     Your user attempted to make a payment but the payment did not complete.
   *             GOV.UK Pay shows an appropriate screen for the failure. Check the payment status error code for more information.
   * cancelled:   Your service cancelled the payment using an API call or the GOV.UK Pay admin tool.
   * error:       Something went wrong with GOV.UK Pay or the underlying payment service provider. Payment fails safely with no money taken.
   *              The user will see a screen stating "We’re experiencing technical problems. No money has been taken from your account.
   *              Cancel and go back to try the payment again."
   */
  CREATED("created", false),
  STARTED("started", false),
  SUBMITTED("submitted", false),
  CAPTURABLE("capturable", false),
  SUCCESS("success", true),
  FAILED("failed", true),
  CANCELLED("cancelled", true),
  ERROR("error", true);

  private final String rawStatus;
  private final boolean journeyFinished;

  GovUkPaymentStatus(String rawStatus, boolean journeyFinished) {
    this.rawStatus = rawStatus;
    this.journeyFinished = journeyFinished;
  }

  @JsonValue
  public String getRawStatus() {
    return rawStatus;
  }

  public boolean isJourneyFinished() {
    return journeyFinished;
  }


}
