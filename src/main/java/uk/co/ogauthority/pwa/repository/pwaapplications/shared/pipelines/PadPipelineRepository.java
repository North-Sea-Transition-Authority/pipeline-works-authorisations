package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer>, PadPipelineDtoRepository {

  List<PadPipeline> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  List<PadPipeline> getAllByPwaApplicationDetailAndIdIn(PwaApplicationDetail detail, List<Integer> padPipelineIds);

  Long countAllByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadPipeline> findByPwaApplicationDetailAndPipeline_Id(PwaApplicationDetail pwaApplicationDetail, Integer pipelineId);

}
