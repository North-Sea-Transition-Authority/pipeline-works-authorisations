package uk.co.ogauthority.pwa.repository.licence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner;

@Repository
public interface PadCrossedBlockOwnerRepository extends CrudRepository<PadCrossedBlockOwner, Integer> {

  List<PadCrossedBlockOwner> findByPadCrossedBlock(PadCrossedBlock padCrossedBlock);

  List<PadCrossedBlockOwner> findByPadCrossedBlockIn(Iterable<PadCrossedBlock> padCrossedBlock);

  List<PadCrossedBlockOwner> findByPadCrossedBlock_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
