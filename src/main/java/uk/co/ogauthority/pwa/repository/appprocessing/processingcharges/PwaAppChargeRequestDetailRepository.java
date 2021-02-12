package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestDetail;

@Repository
public interface PwaAppChargeRequestDetailRepository extends CrudRepository<PwaAppChargeRequestDetail, Integer> {

}