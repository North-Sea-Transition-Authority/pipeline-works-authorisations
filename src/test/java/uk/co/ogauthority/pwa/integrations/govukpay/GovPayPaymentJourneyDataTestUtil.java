package uk.co.ogauthority.pwa.integrations.govukpay;


public final class GovPayPaymentJourneyDataTestUtil {
  private GovPayPaymentJourneyDataTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static GovPayPaymentJourneyData createFrom(String govPayId, GovPayPaymentJourneyState paymentJourneyState) {

    return new GovPayPaymentJourneyData(
        govPayId,
        paymentJourneyState,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );

  }

}