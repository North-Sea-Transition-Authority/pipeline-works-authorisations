package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges;

public final class CreatePaymentAttemptResultTestUtil {

  private CreatePaymentAttemptResultTestUtil() {
    throw new UnsupportedOperationException("not util for you!");
  }

  public static CreatePaymentAttemptResult createSuccess(){
    return new CreatePaymentAttemptResult("some Url", CreatePaymentAttemptResult.AttemptOutcome.PAYMENT_CREATED);
  }

  public static CreatePaymentAttemptResult createCompletedPaymentExists(){
    return new CreatePaymentAttemptResult(null, CreatePaymentAttemptResult.AttemptOutcome.COMPLETED_PAYMENT_EXISTS);
  }
}