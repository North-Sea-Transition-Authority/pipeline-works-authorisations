package uk.co.ogauthority.pwa.repository.migration;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationPwaConsent;

@Repository
public interface MigrationPwaConsentRepository extends CrudRepository<MigrationPwaConsent, Integer> {

  List<MigrationPwaConsent> findByMigrationMasterPwa(MigrationMasterPwa migrationMasterPwa);

}
