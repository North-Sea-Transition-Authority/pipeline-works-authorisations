package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public interface PipelineDetailRepository extends CrudRepository<PipelineDetail, Integer>, PipelineDetailDtoRepository {
  @EntityGraph(attributePaths = {"pipeline", "pipeline.masterPwa"})
  List<PipelineDetail> findAllByPipeline_MasterPwaAndEndTimestampIsNull(MasterPwa masterPwa);

  @EntityGraph(attributePaths = {"pipeline", "pipeline.masterPwa"})
  List<PipelineDetail> findAllByPipeline_MasterPwaAndTipFlag(MasterPwa masterPwa, Boolean tipFlag);

}