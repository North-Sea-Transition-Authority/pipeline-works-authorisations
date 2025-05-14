package uk.co.ogauthority.pwa.repository.pwaconsents;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Repository
public interface PwaConsentRepository extends CrudRepository<PwaConsent, Integer> {

  @EntityGraph(value = "PwaConsent.masterPwaAndSourceApplications")
  List<PwaConsent> findByMasterPwa(MasterPwa masterPwa);

  @EntityGraph(value = "PwaConsent.masterPwaAndSourceApplications")
  List<PwaConsent> findByMasterPwaAndConsentInstantIsAfter(MasterPwa masterPwa, Instant consentedAfterInstant);

  @EntityGraph(value = "PwaConsent.masterPwaAndSourceApplications")
  List<PwaConsent> findByMasterPwaAndConsentInstantIsBefore(MasterPwa masterPwa, Instant consentInstant);

  Optional<PwaConsent> findFirstByMasterPwaOrderByConsentInstantDescVariationNumberDesc(MasterPwa masterPwa);

  Optional<PwaConsent> findBySourcePwaApplication(PwaApplication pwaApplication);

  Optional<PwaConsent> findByReference(String reference);
}
