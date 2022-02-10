package uk.co.ogauthority.pwa.domain.pwa.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;

@Repository
public interface PwaApplicationRepository extends CrudRepository<PwaApplication, Integer> {

  // TODO usages should go through the domain service, rather than direct to the repo.
  //  Or this simply gets moved to be part of the createApp feature.
  @Query(value = "SELECT APP_REF_SEQUENCE.nextval FROM dual", nativeQuery = true)
  Long getNextRefNum();

  List<PwaApplication> findAllByMasterPwa(MasterPwa masterPwa);

}
