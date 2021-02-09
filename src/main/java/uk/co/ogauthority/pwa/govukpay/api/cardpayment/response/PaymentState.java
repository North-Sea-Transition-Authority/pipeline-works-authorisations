package uk.co.ogauthority.pwa.govukpay.api.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.co.ogauthority.pwa.govukpay.GovUkPaymentStatus;

/**
 * A structure representing the current state of the payment in its lifecycle.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentState {


  private GovUkPaymentStatus status;


  private Boolean finished;


  private String message;


  private String code;

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

