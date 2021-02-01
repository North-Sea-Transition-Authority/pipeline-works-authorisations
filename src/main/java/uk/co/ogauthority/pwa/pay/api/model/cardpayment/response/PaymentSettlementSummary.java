package uk.co.ogauthority.pwa.pay.api.model.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A structure representing information about a settlement.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentSettlementSummary {
  @JsonProperty("capture_submit_time")
  private String captureSubmitTime = null;

  @JsonProperty("captured_date")
  private String capturedDate = null;

  @JsonProperty("settled_date")
  private String settledDate = null;

  /**
   * Date and time capture request has been submitted. May be null if capture request was not immediately acknowledged by payment gateway.
   *
   * @return captureSubmitTime
   **/
  public String getCaptureSubmitTime() {
    return captureSubmitTime;
  }

  /**
   * Date of the capture event.
   *
   * @return capturedDate
   **/
  public String getCapturedDate() {
    return capturedDate;
  }

  /**
   * The date that the transaction was paid into the service&#39;s account.
   *
   * @return settledDate
   **/
  public String getSettledDate() {
    return settledDate;
  }

  @Override
  public String toString() {
    return "PaymentSettlementSummary{" +
        "captureSubmitTime='" + captureSubmitTime + '\'' +
        ", capturedDate='" + capturedDate + '\'' +
        ", settledDate='" + settledDate + '\'' +
        '}';
  }
}

