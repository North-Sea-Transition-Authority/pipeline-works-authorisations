package uk.co.ogauthority.pwa.features.appprocessing.issueconsent;

public class IssuedConsentDtoTestUtil {

  private IssuedConsentDtoTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static IssuedConsentDto createDefault(){
    return new IssuedConsentDto("Some consent ref");
  }
}