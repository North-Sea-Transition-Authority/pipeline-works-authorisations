package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCrossedStorageAreaOwnerRepository extends CrudRepository<PadCrossedStorageAreaOwner, Integer> {

  List<PadCrossedStorageAreaOwner> findAllByPadCrossedStorageArea(PadCrossedStorageArea padCrossedStorageArea);

  List<PadCrossedStorageAreaOwner> findAllByPadCrossedStorageAreaIn(Iterable<PadCrossedStorageArea> padCrossedStorageAreas);

  List<PadCrossedStorageAreaOwner> findAllByPadCrossedStorageArea_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
