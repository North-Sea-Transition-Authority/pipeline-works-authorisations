package uk.co.ogauthority.pwa.repository.migration;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;

@Repository
public interface MigrationMasterPwaRepository extends CrudRepository<MigrationMasterPwa, Integer> {
}
