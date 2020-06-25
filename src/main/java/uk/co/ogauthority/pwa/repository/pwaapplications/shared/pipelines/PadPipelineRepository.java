package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer>, PadPipelineDtoRepository {

  List<PadPipeline> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  List<PadPipeline> getAllByPwaApplicationDetailAndIdIn(PwaApplicationDetail detail, List<Integer> ids);

  Long countAllByPwaApplicationDetail(PwaApplicationDetail detail);

  Long countAllByPwaApplicationDetailAndIdIn(PwaApplicationDetail detail, List<Integer> ids);

}
