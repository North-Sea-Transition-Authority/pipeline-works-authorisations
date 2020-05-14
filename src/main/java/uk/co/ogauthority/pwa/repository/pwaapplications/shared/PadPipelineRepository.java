package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;


public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer> {

  List<PadPipeline> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
