package uk.co.ogauthority.pwa.integrations.govukpay;


public final class GovPayPaymentJourneyStateTestUtil {

  private GovPayPaymentJourneyStateTestUtil(){
    throw new UnsupportedOperationException("No util for you!");
  }

  public static GovPayPaymentJourneyState createFor(GovUkPaymentStatus govUkPaymentStatus){
    return new GovPayPaymentJourneyState(govUkPaymentStatus, govUkPaymentStatus.isJourneyFinished(), null, null);
  }

  public static GovPayPaymentJourneyState createFailedJourneyState(String message, String code){
    return new GovPayPaymentJourneyState(GovUkPaymentStatus.FAILED, GovUkPaymentStatus.FAILED.isJourneyFinished(), message, code);
  }


}