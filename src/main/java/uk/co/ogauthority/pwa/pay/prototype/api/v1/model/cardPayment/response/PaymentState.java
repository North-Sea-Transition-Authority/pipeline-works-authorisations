package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A structure representing the current state of the payment in its lifecycle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentState {

  private String status = null;


  private Boolean finished = null;


  private String message = null;


  private String code = null;

  /**
   * Current progress of the payment in its lifecycle
   *
   * @return status
   **/
  public String getStatus() {
    return status;
  }

  /**
   * Whether the payment has finished
   *
   * @return finished
   **/
  public Boolean isFinished() {
    return finished;
  }

  /**
   * What went wrong with the Payment if it finished with an error - English message
   *
   * @return message
   **/
  public String getMessage() {
    return message;
  }

  /**
   * What went wrong with the Payment if it finished with an error - error code
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

