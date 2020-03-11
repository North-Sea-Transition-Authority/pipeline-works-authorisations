package uk.co.ogauthority.pwa.service.pwaconsents;

import java.time.Clock;
import java.time.Instant;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;

@Service
public class PwaConsentService {

  private final PwaConsentRepository pwaConsentRepository;
  private final Clock clock;

  @Autowired
  public PwaConsentService(PwaConsentRepository pwaConsentRepository,
                           @Qualifier("utcClock") Clock clock) {
    this.pwaConsentRepository = pwaConsentRepository;
    this.clock = clock;
  }

  @Transactional
  public PwaConsent createPwaConsentWithoutApplication(MasterPwa masterPwa,
                                                       String reference,
                                                       PwaConsentType pwaConsentType,
                                                       Instant consentedInstant,
                                                       boolean isMigrated) {
    var pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(masterPwa);
    pwaConsent.setConsentType(pwaConsentType);
    pwaConsent.setMigratedFlag(isMigrated);
    pwaConsent.setCreatedInstant(clock.instant());
    pwaConsent.setConsentInstant(consentedInstant);
    pwaConsent.setReference(reference);
    pwaConsentRepository.save(pwaConsent);
    return pwaConsent;
  }

}
