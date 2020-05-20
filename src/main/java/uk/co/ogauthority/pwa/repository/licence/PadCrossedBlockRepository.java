package uk.co.ogauthority.pwa.repository.licence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;

@Repository
public interface PadCrossedBlockRepository extends CrudRepository<PadCrossedBlock, Integer> {

  List<PadCrossedBlock> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerIn(PwaApplicationDetail pwaApplicationDetail,
                                                                Iterable<CrossedBlockOwner> blockOwners);

  int countPadCrossedBlockByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
