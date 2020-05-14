package uk.co.ogauthority.pwa.repository.masterpwas;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@Repository
public interface MasterPwaDetailRepository extends CrudRepository<MasterPwaDetail, Integer> {

  @EntityGraph(attributePaths = {"masterPwa"})
  List<MasterPwaDetail> findByEndInstantIsNullAndMasterPwaDetailStatus(MasterPwaDetailStatus masterPwaDetailStatus);
}
