package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCrossedBlockRepository extends CrudRepository<PadCrossedBlock, Integer> {

  List<PadCrossedBlock> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  boolean existsByIdAndPwaApplicationDetail(Integer id, PwaApplicationDetail pwaApplicationDetail);

  int countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerIn(PwaApplicationDetail pwaApplicationDetail,
                                                                Iterable<CrossedBlockOwner> blockOwners);

  int countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(PwaApplicationDetail pwaApplicationDetail,
                                                                CrossedBlockOwner blockOwners);

  int countPadCrossedBlockByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countPadCrossedBlockByPwaApplicationDetailAndBlockReference(PwaApplicationDetail pwaApplicationDetail, String blockReference);

}
