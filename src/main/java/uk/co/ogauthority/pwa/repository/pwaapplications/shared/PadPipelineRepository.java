package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

import java.util.List;

public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer> {

  List<PadPipeline> findAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
