package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadCrossedStorageAreaRepository extends CrudRepository<PadCrossedStorageArea, Integer> {

  List<PadCrossedStorageArea> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  boolean existsByIdAndPwaApplicationDetail(Integer id, PwaApplicationDetail pwaApplicationDetail);

  int countPadCrossedStorageAreaByPwaApplicationDetailAndCrossingOwnerTypeIn(
      PwaApplicationDetail pwaApplicationDetail,
      Iterable<CrossingOwner> crossingOwnerTypes);

  int countPadCrossedStorageAreaByPwaApplicationDetailAndStorageAreaReferenceIgnoreCase(
      PwaApplicationDetail pwaApplicationDetail,
      String storageAreaReference);

  int countPadCrossedStorageAreaByPwaApplicationDetailAndCrossingOwnerTypeNot(
      PwaApplicationDetail pwaApplicationDetail,
      CrossingOwner crossingOwnerType);

  int countPadCrossedStorageAreaByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
