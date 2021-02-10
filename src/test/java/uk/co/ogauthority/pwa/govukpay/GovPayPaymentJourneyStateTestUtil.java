package uk.co.ogauthority.pwa.govukpay;


public final class GovPayPaymentJourneyStateTestUtil {

  private GovPayPaymentJourneyStateTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static GovPayPaymentJourneyState createFor(GovUkPaymentStatus govUkPaymentStatus){
    return new GovPayPaymentJourneyState(govUkPaymentStatus, govUkPaymentStatus.isJourneyFinished(), null, null);
  }


}