package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.FeePeriod;

@Repository
public interface FeePeriodRepository extends CrudRepository<FeePeriod, Integer> {

}