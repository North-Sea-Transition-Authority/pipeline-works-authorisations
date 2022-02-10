package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PwaAppChargeRequestItemRepository extends CrudRepository<PwaAppChargeRequestItem, Integer> {

  List<PwaAppChargeRequestItem> findAllByPwaAppChargeRequestOrderByDescriptionAsc(PwaAppChargeRequest pwaAppChargeRequest);

}