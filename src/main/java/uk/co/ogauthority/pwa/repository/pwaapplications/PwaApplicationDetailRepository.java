package uk.co.ogauthority.pwa.repository.pwaapplications;

import java.util.Collection;
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

  List<PwaApplicationDetail> findByPwaApplicationAndSubmittedTimestampIsNotNull(PwaApplication pwaApplication);

  List<PwaApplicationDetail> findByPwaApplicationAndStatus(PwaApplication pwaApplication, PwaApplicationStatus status);

  List<PwaApplicationDetail> findByPwaApplicationId(int applicationId);

  @Query("SELECT pad FROM PwaApplicationDetail pad " +
      "JOIN PadVersionLookup psv ON psv.pwaApplicationId = pad.pwaApplication.id " +
      "AND psv.latestSubmittedTimestamp = pad.submittedTimestamp " +
      // manual JOIN FETCH prevents N queries to fetch the app and master pwa for each detail by inlining those details
      "JOIN FETCH pad.pwaApplication pa " +
      "JOIN FETCH pa.masterPwa mp " +
      "WHERE pad.status in :status")
  List<PwaApplicationDetail> findLastSubmittedAppDetailsWithStatusIn(Collection<PwaApplicationStatus> status);

  List<PwaApplicationDetail> findByPwaApplication(PwaApplication pwaApplication);

}