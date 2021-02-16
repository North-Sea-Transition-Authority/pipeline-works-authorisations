package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriodDetail;

@Repository
public interface FeePeriodDetailRepository extends CrudRepository<FeePeriodDetail, Integer> {


  @Query(value = "FROM FeePeriodDetail fpd " +
      "WHERE fpd.tipFlag = TRUE " +
      "AND ( " +
      "  fpd.periodStartTimestamp <= :feePeriodContainsInstant " +
      "  AND " +
      "  COALESCE(fpd.periodEndTimestamp, :feePeriodEndOverrideIfNull) >= :feePeriodContainsInstant " +
      ")")
  Optional<FeePeriodDetail> findByTipFlagIsTrueAndPeriodStartTimestampIsBeforeAndPeriodEndTimestampIsAfter(
      @Param(value = "feePeriodContainsInstant") Instant feePeriodContainsInstant,
      @Param(value = "feePeriodEndOverrideIfNull") Instant feePeriodEndOverrideIfNull
  );


}