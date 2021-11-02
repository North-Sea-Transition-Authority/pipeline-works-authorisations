package uk.co.ogauthority.pwa.integrations.govukpay;

/**
 * Interface implemented by services providing integration with the card payments branch of the gov uk pay API.
 */
public interface GovUkPayCardPaymentClient {

  GovPayNewCardPaymentResult createCardPaymentJourney(GovPayNewCardPaymentRequest govPayNewCardPaymentRequest);


  GovPayPaymentJourneyData getCardPaymentJourneyData(String paymentId);

  void cancelCardPaymentJourney(String paymentId);
}
