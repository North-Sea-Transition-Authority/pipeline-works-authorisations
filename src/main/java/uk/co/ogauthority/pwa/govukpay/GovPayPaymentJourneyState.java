package uk.co.ogauthority.pwa.govukpay;

/**
 * A structure representing the current state of the payment in its lifecycle.
 */
public final class GovPayPaymentJourneyState {

  private final GovUkPaymentStatus status;

  private final boolean finished;

  private final String message;

  private final String code;

  GovPayPaymentJourneyState(GovUkPaymentStatus status, boolean finished, String message, String code) {
    this.status = status;
    this.finished = finished;
    this.message = message;
    this.code = code;
  }

  /**
   * Current progress of the payment in its lifecycle.
   *
   * @return status
   **/
  public GovUkPaymentStatus getStatus() {
    return status;
  }

  /**
   * Whether the payment has finished.
   *
   * @return finished
   **/
  public boolean isFinished() {
    return finished;
  }

  /**
   * What went wrong with the Payment if it finished with an error - English message.
   *
   * @return message
   **/
  public String getMessage() {
    return message;
  }

  /**
   * What went wrong with the Payment if it finished with an error - error code.
   *
   * @return code
   **/
  public String getCode() {
    return code;
  }


  @Override
  public String toString() {
    return "PaymentState{" +
        "status='" + status + '\'' +
        ", finished=" + finished +
        ", message='" + message + '\'' +
        ", code='" + code + '\'' +
        '}';
  }
}

