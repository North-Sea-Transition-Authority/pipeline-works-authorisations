package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PwaConsentTestUtil {

  private PwaConsentTestUtil() {
    throw new UnsupportedOperationException("No util for you!");
  }

  public static PwaConsent createInitial(MasterPwa masterPwa){

    var consent = new PwaConsent();
    consent.setMasterPwa(masterPwa);
    consent.setConsentType(PwaConsentType.INITIAL_PWA);
    return consent;

  }

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

  public static PwaConsent createPwaConsentWithCompleteApplicationDetail(int id, String reference, Instant consentInstant, PwaApplicationDetail applicationDetail) {
    var pwaConsent = new PwaConsent();
    pwaConsent.setId(id);
    pwaConsent.setReference(reference);
    pwaConsent.setConsentInstant(consentInstant);
    pwaConsent.setMasterPwa(applicationDetail.getMasterPwa());
    pwaConsent.setSourcePwaApplication(applicationDetail.getPwaApplication());
    return pwaConsent;
  }
}