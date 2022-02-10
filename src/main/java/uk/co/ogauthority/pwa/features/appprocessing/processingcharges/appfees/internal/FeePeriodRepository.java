package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.internal;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeePeriodRepository extends CrudRepository<FeePeriod, Integer> {

}