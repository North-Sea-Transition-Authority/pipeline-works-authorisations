package uk.co.ogauthority.pwa.support.paydashboard;

import uk.co.ogauthority.pwa.pwapay.PwaPaymentRequest;

public class PaymentRequestView {

  private final PwaPaymentRequest paymentRequest;
  private final String viewUrl;

  public PaymentRequestView(PwaPaymentRequest paymentRequest, String viewUrl) {
    this.paymentRequest = paymentRequest;
    this.viewUrl = viewUrl;
  }

  public PwaPaymentRequest getPaymentRequest() {
    return paymentRequest;
  }

  public String getViewUrl() {
    return viewUrl;
  }
}
