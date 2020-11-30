package uk.co.ogauthority.pwa.repository.pwaapplications;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Repository
public interface PwaApplicationDetailRepository extends CrudRepository<PwaApplicationDetail, Integer> {

  /**
   * Find current version of PWA application detail if status matches passed-in status.
   */
  Optional<PwaApplicationDetail> findByPwaApplicationIdAndStatusAndTipFlagIsTrue(Integer pwaApplicationId,
                                                                                 PwaApplicationStatus status);

  Optional<PwaApplicationDetail> findByPwaApplicationIdAndTipFlagIsTrue(Integer pwaApplicationId);

  @Query(
      "SELECT pad " +
          "FROM PwaApplicationDetail pad " +
          "JOIN PwaApplicationStatusCategoryLookup pascl ON pad.pwaApplication.id = pascl.pwaApplicationId " +
          "AND pascl.lastSubmittedVersion = pad.versionNo " +
          "WHERE pad.pwaApplication.id = :pwaApplicationId "
  )
  Optional<PwaApplicationDetail> findLastSubmittedApplicationDetail(Integer pwaApplicationId);

  List<PwaApplicationDetail> findByPwaApplicationAndSubmittedTimestampIsNotNull(PwaApplication pwaApplication);

}
