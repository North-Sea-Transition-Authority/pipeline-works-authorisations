package uk.co.ogauthority.pwa.service.pwaconsents;

import java.time.LocalDate;

public interface PwaConsentReferenceNumberGenerator {

  int getConsentNumber(LocalDate consentDate);

}
