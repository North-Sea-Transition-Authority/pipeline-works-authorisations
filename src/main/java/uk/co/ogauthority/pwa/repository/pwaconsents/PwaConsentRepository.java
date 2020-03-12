package uk.co.ogauthority.pwa.repository.pwaconsents;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;

@Repository
public interface PwaConsentRepository extends CrudRepository<PwaConsent, Integer> {
}
