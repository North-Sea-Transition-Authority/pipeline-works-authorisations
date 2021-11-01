package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Repository
public interface PadPipelineCrossingOwnerRepository extends CrudRepository<PadPipelineCrossingOwner, Integer> {

  List<PadPipelineCrossingOwner> findAllByPadPipelineCrossing(PadPipelineCrossing padPipelineCrossing);

  List<PadPipelineCrossingOwner> findAllByPadPipelineCrossing_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
