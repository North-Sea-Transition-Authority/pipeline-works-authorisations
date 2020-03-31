package uk.co.ogauthority.pwa.repository.licence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.licence.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCrossedBlockRepository extends CrudRepository<PadCrossedBlock, Integer> {

  List<PadCrossedBlock> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
