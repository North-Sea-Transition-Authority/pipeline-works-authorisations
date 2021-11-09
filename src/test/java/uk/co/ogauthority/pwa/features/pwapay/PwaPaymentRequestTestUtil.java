package uk.co.ogauthority.pwa.features.pwapay;


import java.util.UUID;

public final class PwaPaymentRequestTestUtil {

  private PwaPaymentRequestTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PwaPaymentRequest createFrom(UUID uuid, PaymentRequestStatus requestStatus, String govPayId){
    var paymentRequest = new PwaPaymentRequest();
    paymentRequest.setUuid(uuid);
    paymentRequest.setRequestStatus(requestStatus);
    paymentRequest.setGovUkPaymentId(govPayId);
    return paymentRequest;
  }
}