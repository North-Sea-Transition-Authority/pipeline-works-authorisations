package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;


import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;

public class ParallelConsentViewTestUtil {

  private ParallelConsentViewTestUtil() {
    throw new UnsupportedOperationException("no util for you!");
  }

  public static ParallelConsentView createParallelConsentView(int pwaConsentId, PwaApplicationType pwaApplicationType, int pwaApplicationId){
    return new ParallelConsentView(
        pwaConsentId,
        "Consent Ref: " + pwaConsentId,
        1,
        PwaApplicationType.INITIAL,
        "Application Ref: " + pwaApplicationId,
        null,
        "");

  }

}