package uk.co.ogauthority.pwa.repository.migration;

import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationPwaConsent;
import uk.co.ogauthority.pwa.model.entity.migration.ProcessedPwaConsentMigration;

@Repository
public interface ProcessedPwaConsentMigrationRepository extends CrudRepository<ProcessedPwaConsentMigration, Integer> {

  boolean existsByMigrationPwaConsentIn(Set<MigrationPwaConsent> pwaConsents);
}
