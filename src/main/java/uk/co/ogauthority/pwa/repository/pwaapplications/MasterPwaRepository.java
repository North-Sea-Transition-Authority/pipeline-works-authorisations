package uk.co.ogauthority.pwa.repository.pwaapplications;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwa.MasterPwa;

@Repository
public interface MasterPwaRepository extends CrudRepository<MasterPwa, Integer> {
}
