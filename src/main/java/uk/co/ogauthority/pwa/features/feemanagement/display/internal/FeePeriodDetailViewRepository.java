package uk.co.ogauthority.pwa.features.feemanagement.display.internal;

import java.time.Instant;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeePeriodDetailViewRepository extends CrudRepository<DisplayableFeePeriodDetail, Integer> {
  List<DisplayableFeePeriodDetail> findAllByPeriodStartTimestampAfter(Instant periodStartTimestamp);

}
