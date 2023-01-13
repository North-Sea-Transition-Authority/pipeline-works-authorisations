package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineRepository extends CrudRepository<PadPipeline, Integer>, PadPipelineDtoRepository {

  List<PadPipeline> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  List<PadPipeline> getAllByPwaApplicationDetailAndIdIn(PwaApplicationDetail detail, List<Integer> padPipelineIds);

  Long countAllByPwaApplicationDetail(PwaApplicationDetail detail);

  Optional<PadPipeline> findByPwaApplicationDetailAndPipeline_Id(PwaApplicationDetail pwaApplicationDetail, Integer pipelineId);

  List<PadPipeline> findAllByPipelineIn(List<Pipeline> pipelines);

}
