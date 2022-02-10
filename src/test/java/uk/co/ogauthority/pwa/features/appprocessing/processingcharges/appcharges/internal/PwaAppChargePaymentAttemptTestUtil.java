package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;

import java.time.Instant;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import uk.co.ogauthority.pwa.features.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.features.pwapay.PwaPaymentRequestTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;

public final class PwaAppChargePaymentAttemptTestUtil {

  private PwaAppChargePaymentAttemptTestUtil(){
    throw new UnsupportedOperationException("no util for you!");
  }


  public static PwaAppChargePaymentAttempt createWithPaymentRequest(PwaAppChargeRequest pwaAppChargeRequest,
                                                                    PaymentRequestStatus paymentRequestStatus,
                                                                    Person person){

    var paymentRequest = PwaPaymentRequestTestUtil.createFrom(
        UUID.randomUUID(),
        paymentRequestStatus,
        RandomStringUtils.randomAlphabetic(10)
    );

    return new PwaAppChargePaymentAttempt(
      pwaAppChargeRequest, person.getId(), Instant.now(), true, paymentRequest
    );

  }



}