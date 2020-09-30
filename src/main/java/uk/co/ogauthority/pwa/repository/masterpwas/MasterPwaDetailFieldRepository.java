package uk.co.ogauthority.pwa.repository.masterpwas;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetailField;

@Repository
public interface MasterPwaDetailFieldRepository extends CrudRepository<MasterPwaDetailField, Integer> {

  @EntityGraph(attributePaths = {"masterPwaDetail"})
  List<MasterPwaDetailField> findByMasterPwaDetail(MasterPwaDetail masterPwaDetail);


}
