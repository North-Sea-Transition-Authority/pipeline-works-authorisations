package uk.co.ogauthority.pwa.model.entity.pwaconsents;

import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

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
}