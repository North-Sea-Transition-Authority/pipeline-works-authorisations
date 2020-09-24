package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;

@Repository
public interface PadPipelineCrossingOwnerRepository extends CrudRepository<PadPipelineCrossingOwner, Integer> {

  List<PadPipelineCrossingOwner> findAllByPadPipelineCrossing(PadPipelineCrossing padPipelineCrossing);

  List<PadPipelineCrossingOwner> findAllByPadPipelineCrossing_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
