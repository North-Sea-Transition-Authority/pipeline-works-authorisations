package uk.co.ogauthority.pwa.pay;

/**
 * A structure representing the current state of the payment in its lifecycle.
 */
public final class PaymentJourneyState {

  private final String status;

  private final Boolean finished;

  private final String message;

  private final String code;

  PaymentJourneyState(String status, Boolean finished, String message, String code) {
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
  public String getStatus() {
    return status;
  }

  /**
   * Whether the payment has finished.
   *
   * @return finished
   **/
  public Boolean isFinished() {
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

