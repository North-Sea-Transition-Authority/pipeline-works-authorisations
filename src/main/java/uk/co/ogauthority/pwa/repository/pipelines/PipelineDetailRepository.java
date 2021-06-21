package uk.co.ogauthority.pwa.repository.pipelines;


import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

public interface PipelineDetailRepository extends CrudRepository<PipelineDetail, Integer>, PipelineDetailDtoRepository {
  @EntityGraph(attributePaths = {"pipeline", "pipeline.masterPwa"})
  List<PipelineDetail> findAllByPipeline_MasterPwaAndEndTimestampIsNull(MasterPwa masterPwa);

  @EntityGraph(attributePaths = {"pipeline", "pipeline.masterPwa"})
  List<PipelineDetail> findAllByPipeline_MasterPwaAndPipelineStatusIsNotInAndTipFlagIsTrue(MasterPwa masterPwa,
                                                                                           Set<PipelineStatus> statusNot);

  Optional<PipelineDetail> getByPipeline_IdAndTipFlagIsTrue(Integer pipelineId);

  boolean existsByPipeline_IdAndTipFlagIsTrue(Integer pipelineId);

  List<PipelineDetail> findAllByPipeline_Id(Integer pipelineId);

  List<PipelineDetail> findAllByPipeline_IdInAndTipFlagIsTrue(List<Integer> pipelineIds);

  List<PipelineDetail> findAllByPipelineInAndEndTimestampIsNull(Collection<Pipeline> pipelines);

}