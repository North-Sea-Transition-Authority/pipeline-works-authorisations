package uk.co.ogauthority.pwa.repository.masterpwas;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailArea;

@Repository
public interface MasterPwaDetailAreaRepository extends CrudRepository<MasterPwaDetailArea, Integer> {

  @EntityGraph(attributePaths = {"masterPwaDetail"})
  List<MasterPwaDetailArea> findByMasterPwaDetail(MasterPwaDetail masterPwaDetail);


}
