package uk.co.ogauthority.pwa.integrations.govukpay.api.cardpayment.response;/*

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * A structure representing the refunds availability.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundSummary {
  @JsonProperty("status")
  private String status = null;

  @JsonProperty("amount_available")
  private Long amountAvailable = null;

  @JsonProperty("amount_submitted")
  private Long amountSubmitted = null;

  public RefundSummary status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Availability status of the refund.
   *
   * @return status
   **/
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Amount available for refund in pence.
   *
   * @return amountAvailable
   **/
  public Long getAmountAvailable() {
    return amountAvailable;
  }

  /**
   * Amount submitted for refunds on this Payment in pence.
   *
   * @return amountSubmitted
   **/
  public Long getAmountSubmitted() {
    return amountSubmitted;
  }

  @Override
  public String toString() {
    return "RefundSummary{" +
        "status='" + status + '\'' +
        ", amountAvailable=" + amountAvailable +
        ", amountSubmitted=" + amountSubmitted +
        '}';
  }
}

