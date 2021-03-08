package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;

@Repository
public interface PwaAppChargeRequestRepository extends CrudRepository<PwaAppChargeRequest, Integer> {

}