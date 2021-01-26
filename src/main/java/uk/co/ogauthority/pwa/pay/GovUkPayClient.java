package uk.co.ogauthority.pwa.pay;

public interface GovUkPayClient {

  NewCardPaymentResult createCardPaymentJourney(NewCardPaymentRequest newCardPaymentRequest);
}
