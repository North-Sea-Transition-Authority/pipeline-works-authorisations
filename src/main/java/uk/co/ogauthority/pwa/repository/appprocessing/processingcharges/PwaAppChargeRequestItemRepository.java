package uk.co.ogauthority.pwa.repository.appprocessing.processingcharges;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargeRequestItem;

@Repository
public interface PwaAppChargeRequestItemRepository extends CrudRepository<PwaAppChargeRequestItem, Integer> {

  List<PwaAppChargeRequestItem> findAllByPwaAppChargeRequestOrderByDescriptionAsc(PwaAppChargeRequest pwaAppChargeRequest);

}