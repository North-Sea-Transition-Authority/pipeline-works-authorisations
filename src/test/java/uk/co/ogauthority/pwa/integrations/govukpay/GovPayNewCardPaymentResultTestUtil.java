package uk.co.ogauthority.pwa.integrations.govukpay;

import java.time.LocalDateTime;
import java.util.Map;

public final class GovPayNewCardPaymentResultTestUtil {

  public static final String NEXT_URL_BASE = "/fake-pay/next/";

  private GovPayNewCardPaymentResultTestUtil() {
    throw new UnsupportedOperationException("No util for You!");
  }

  public static GovPayNewCardPaymentResult createFrom(String govPayId,
                                                      GovUkPaymentStatus govUkPaymentStatus,
                                                      String returnUrl) {
    return new GovPayNewCardPaymentResult(
        govPayId,
        GovPayPaymentJourneyStateTestUtil.createFor(govUkPaymentStatus),
        returnUrl,
        NEXT_URL_BASE + govPayId,
        LocalDateTime.now(),
        Map.of()
    );
  }

}