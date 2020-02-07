package uk.co.ogauthority.pwa.repository.pwaapplications;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplication;

@Repository
public interface PwaApplicationRepository extends CrudRepository<PwaApplication, Integer> {
}
