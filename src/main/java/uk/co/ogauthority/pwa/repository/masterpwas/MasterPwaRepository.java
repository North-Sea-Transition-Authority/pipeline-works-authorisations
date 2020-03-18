package uk.co.ogauthority.pwa.repository.masterpwas;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Repository
public interface MasterPwaRepository extends CrudRepository<MasterPwa, Integer> {
}
