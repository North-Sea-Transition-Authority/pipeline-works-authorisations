package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil;

import java.time.Instant;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PwaViewTabTestUtil {


  private PwaViewTabTestUtil(){}


  public static PwaConsentApplicationDto createConsentApplicationDto(Instant consentInstant) {
    return new PwaConsentApplicationDto(
        1, consentInstant, "consent ref", 1, PwaApplicationType.INITIAL, "app ref");
  }



}
