package uk.co.ogauthority.pwa.repository.pwaapplications;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

@Repository
public interface PwaApplicationRepository extends CrudRepository<PwaApplication, Integer> {

  @Query(value = "SELECT APP_REF_SEQUENCE.nextval FROM dual", nativeQuery = true)
  Long getNextRefNum();


}
