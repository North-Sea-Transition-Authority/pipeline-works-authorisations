package uk.co.ogauthority.pwa.govukpay;

/**
 * Interface implemented by services providing integration with the card payments branch of the gov uk pay API.
 */
public interface GovUkPayCardPaymentClient {

  NewCardPaymentResult createCardPaymentJourney(NewCardPaymentRequest newCardPaymentRequest);


  PaymentJourneyData getCardPaymentJourneyData(String paymentId);

  void cancelCardPaymentJourney(String paymentId);
}
