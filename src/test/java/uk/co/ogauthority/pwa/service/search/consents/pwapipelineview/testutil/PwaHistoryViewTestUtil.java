package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

public class PwaHistoryViewTestUtil {


  private PwaHistoryViewTestUtil(){}


  public static PwaConsent createPwaConsent(int id, String reference, Instant consentInstant) {
    var pwaConsent = new PwaConsent();
    pwaConsent.setId(id);
    pwaConsent.setReference(reference);
    pwaConsent.setConsentInstant(consentInstant);
    return pwaConsent;
  }

  public static PwaConsent createPwaConsent(int id, String reference, Instant consentInstant, int variantNumber) {
    var pwaConsent = createPwaConsent(id, reference, consentInstant);
    pwaConsent.setVariationNumber(variantNumber);
    return pwaConsent;
  }




}
