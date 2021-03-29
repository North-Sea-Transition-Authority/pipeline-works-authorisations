package uk.co.ogauthority.pwa.service.pwaconsents;

import java.time.Clock;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;

@Service
public class PwaConsentService {

  private final PwaConsentRepository pwaConsentRepository;
  private final Clock clock;
  private final PwaConsentReferencingService pwaConsentReferencingService;

  @Autowired
  public PwaConsentService(PwaConsentRepository pwaConsentRepository,
                           @Qualifier("utcClock") Clock clock,
                           PwaConsentReferencingService pwaConsentReferencingService) {
    this.pwaConsentRepository = pwaConsentRepository;
    this.clock = clock;
    this.pwaConsentReferencingService = pwaConsentReferencingService;
  }

  @Transactional
  public PwaConsent createConsent(PwaApplication application) {

    var pwaConsent = new PwaConsent();
    pwaConsent.setMasterPwa(application.getMasterPwa());
    pwaConsent.setSourcePwaApplication(application);
    pwaConsent.setConsentType(application.getApplicationType().getPwaConsentType());
    pwaConsent.setMigratedFlag(false);
    pwaConsent.setCreatedInstant(clock.instant());
    pwaConsent.setConsentInstant(clock.instant());

    // only set the variation number if not a deposit consent, DEPCONs have null variation number
    if (pwaConsent.getConsentType() != PwaConsentType.DEPOSIT_CONSENT) {

      // add one to the max previous variation number to get our new number, or start at 0
      var variationNumber = pwaConsentRepository.findByMasterPwa(application.getMasterPwa()).stream()
          .filter(consent -> consent.getVariationNumber() != null)
          .mapToInt(PwaConsent::getVariationNumber)
          .max()
          .stream()
          .map(v -> v + 1)
          .findFirst()
          .orElse(0);

      pwaConsent.setVariationNumber(variationNumber);

    }

    var ref = pwaConsentReferencingService.createConsentReference(pwaConsent);
    pwaConsent.setReference(ref);

    return pwaConsentRepository.save(pwaConsent);

  }

  public List<PwaConsent> getConsentsByMasterPwa(MasterPwa masterPwa) {
    return pwaConsentRepository.findByMasterPwa(masterPwa);
  }

}
