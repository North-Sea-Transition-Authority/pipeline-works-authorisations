package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.testutil;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class PwaViewTabTestUtil {

  private PwaViewTabTestUtil(){
    throw new AssertionError("Can't instantiate this");
  }


  public static PwaConsentApplicationDto createMigratedConsentApplicationDto(Instant consentInstant) {
    return new PwaConsentApplicationDto(
        1, consentInstant, "consent ref", 1, PwaApplicationType.INITIAL, "app ref",
        null, null);
  }

  public static PwaConsentApplicationDto createConsentApplicationDto(Instant consentInstant, DocgenRun docgenRun) {
    return new PwaConsentApplicationDto(
        1, consentInstant, "consent ref", 1, PwaApplicationType.INITIAL, "app ref",
        docgenRun.getId(), docgenRun.getStatus());
  }

}
