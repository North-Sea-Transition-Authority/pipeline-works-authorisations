package uk.co.ogauthority.pwa.pwapay;

public final class CreateCardPaymentResultTestUtil {

  private CreateCardPaymentResultTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }


  public static CreateCardPaymentResult createWithUrl(PwaPaymentRequest paymentRequest){

    return new CreateCardPaymentResult(
        paymentRequest, "SomeUrl"
    );

  }

  public static CreateCardPaymentResult createWithoutUrl(PwaPaymentRequest paymentRequest){

    return new CreateCardPaymentResult(
        paymentRequest, null
    );

  }

}