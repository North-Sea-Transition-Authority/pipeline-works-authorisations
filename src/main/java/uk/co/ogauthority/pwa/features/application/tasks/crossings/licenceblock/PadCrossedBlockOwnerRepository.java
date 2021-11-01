package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCrossedBlockOwnerRepository extends CrudRepository<PadCrossedBlockOwner, Integer> {

  @EntityGraph(attributePaths = { "padCrossedBlock" })
  List<PadCrossedBlockOwner> findByPadCrossedBlock(PadCrossedBlock padCrossedBlock);

  @EntityGraph(attributePaths = { "padCrossedBlock" })
  List<PadCrossedBlockOwner> findByPadCrossedBlockIn(Iterable<PadCrossedBlock> padCrossedBlock);

  @EntityGraph(attributePaths = { "padCrossedBlock" })
  List<PadCrossedBlockOwner> findByPadCrossedBlock_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
