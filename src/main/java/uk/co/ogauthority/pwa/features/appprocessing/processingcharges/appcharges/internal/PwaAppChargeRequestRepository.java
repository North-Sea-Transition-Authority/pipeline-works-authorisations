package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PwaAppChargeRequestRepository extends CrudRepository<PwaAppChargeRequest, Integer> {

}