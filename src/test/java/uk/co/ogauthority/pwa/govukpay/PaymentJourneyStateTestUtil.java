package uk.co.ogauthority.pwa.govukpay;


public final class PaymentJourneyStateTestUtil {

  private PaymentJourneyStateTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PaymentJourneyState createFor(GovUkPaymentStatus govUkPaymentStatus){
    return new PaymentJourneyState(govUkPaymentStatus, govUkPaymentStatus.isJourneyFinished(), null, null);
  }


}