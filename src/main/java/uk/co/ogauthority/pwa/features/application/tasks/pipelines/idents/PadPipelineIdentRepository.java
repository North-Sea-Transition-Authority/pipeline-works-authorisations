package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineIdentRepository extends CrudRepository<PadPipelineIdent, Integer> {

  Long countAllByPadPipeline(PadPipeline pipeline);

  @EntityGraph(attributePaths = "padPipeline")
  Optional<PadPipelineIdent> findTopByPadPipelineOrderByIdentNoDesc(PadPipeline pipeline);

  // Due to a bug within Hibernate, using a repository method with an EntityGraph within the same transaction
  // as updating that data causes issues (see EDU-7096). Replacing the EntityGraph with the Query will resolve
  // this problem until Hibernate is fixed
  @Query("""
        FROM PadPipelineIdent ppi
        JOIN PadPipeline pp ON ppi.padPipeline = pp
        WHERE pp = :pipeline
      """)
  List<PadPipelineIdent> getAllByPadPipeline(PadPipeline pipeline);

  @EntityGraph(attributePaths = "padPipeline")
  Optional<PadPipelineIdent> getPadPipelineIdentByPadPipelineAndId(PadPipeline pipeline, Integer identId);

  @EntityGraph(attributePaths = "padPipeline")
  Optional<PadPipelineIdent> getByPadPipelineAndAndIdentNo(PadPipeline pipeline, Integer identNo);

  @EntityGraph(attributePaths = "padPipeline")
  List<PadPipelineIdent> getAllByPadPipeline_IdIn(List<Integer> padPipelineIds);

  @EntityGraph(attributePaths = "padPipeline")
  List<PadPipelineIdent> getAllByPadPipeline_PwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  @EntityGraph(attributePaths = "padPipeline")
  List<PadPipelineIdent> getAllByPadPipeline_Pipeline_IdInAndPadPipeline_PwaApplicationDetail(Collection<Integer> pipelineIds,
                                                                                              PwaApplicationDetail pwaApplicationDetail);

}
