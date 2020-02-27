package uk.co.ogauthority.pwa.repository.masterpwa;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;

@Repository
public interface MasterPwaDetailRepository extends CrudRepository<MasterPwaDetail, Integer> {

  List<MasterPwaDetail> findByEndInstantIsNullAndMasterPwaDetailStatus(MasterPwaDetailStatus masterPwaDetailStatus);
}
