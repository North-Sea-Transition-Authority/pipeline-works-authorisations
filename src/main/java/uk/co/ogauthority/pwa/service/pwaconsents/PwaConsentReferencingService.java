package uk.co.ogauthority.pwa.service.pwaconsents;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class PwaConsentReferencingService {

  private final PwaConsentReferenceNumberGenerator pwaConsentReferenceNumberGenerator;

  @Autowired
  public PwaConsentReferencingService(PwaConsentReferenceNumberGenerator pwaConsentReferenceNumberGenerator) {
    this.pwaConsentReferenceNumberGenerator = pwaConsentReferenceNumberGenerator;
  }

  @Transactional
  public String createConsentReference(PwaConsent pwaConsent) {

    var consentDate = LocalDate.ofInstant(pwaConsent.getConsentInstant(), ZoneOffset.UTC);

    var consentNumber = pwaConsentReferenceNumberGenerator.getConsentNumber(consentDate);

    var refLetter = pwaConsent.getSourcePwaApplication().getApplicationType().getPwaConsentType().getRefLetter();

    return constructReference(consentNumber, refLetter, consentDate);

  }

  private String constructReference(int consentNumber, String refLetter, LocalDate consentDate) {
    return String.format("%s/%s/%s", consentNumber, refLetter, DateUtils.getTwoDigitYear(consentDate));
  }

}
