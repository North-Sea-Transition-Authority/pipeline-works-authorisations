package uk.co.ogauthority.pwa.service.pwaconsents;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.EntityLatestVersionNotFoundException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
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

  public PwaConsent getConsentById(Integer consentId) {
    return pwaConsentRepository.findById(consentId).orElseThrow(
        () -> new PwaEntityNotFoundException("Pwa Consent could not be found for consent id: " + consentId));
  }

  public List<PwaConsent> getPwaConsentsWhereConsentInstantAfter(MasterPwa masterPwa, Instant searchStartInstant) {
    return pwaConsentRepository.findByMasterPwaAndConsentInstantIsAfter(masterPwa, searchStartInstant);
  }

  public List<PwaConsent> getPwaConsentsWhereConsentInstantBefore(MasterPwa masterPwa, Instant searchStartInstant) {
    return pwaConsentRepository.findByMasterPwaAndConsentInstantIsBefore(masterPwa, searchStartInstant);
  }

  public PwaConsent getLatestConsent(MasterPwa masterPwa) {
    return pwaConsentRepository.findFirstByMasterPwaOrderByConsentInstantDescVariationNumberDesc(masterPwa)
        .orElseThrow(() -> new EntityLatestVersionNotFoundException(
            "The latest consent could not be found for master pwa with id: " + masterPwa.getId()));
  }

  public Optional<PwaConsent> getConsentByPwaApplication(PwaApplication pwaApplication) {
    return pwaConsentRepository.findBySourcePwaApplication(pwaApplication);
  }

  public void setDocgenRunId(PwaConsent consent, DocgenRun docgenRun) {
    consent.setDocgenRunId(docgenRun.getId());
    pwaConsentRepository.save(consent);
  }

}
