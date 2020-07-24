package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;

public interface PadPipelineIdentRepository extends CrudRepository<PadPipelineIdent, Integer> {

  Long countAllByPadPipeline(PadPipeline pipeline);

  Optional<PadPipelineIdent> findTopByPadPipelineOrderByIdentNoDesc(PadPipeline pipeline);

  @EntityGraph(attributePaths = "padPipeline")
  List<PadPipelineIdent> getAllByPadPipeline(PadPipeline pipeline);

  Optional<PadPipelineIdent> getPadPipelineIdentByPadPipelineAndId(PadPipeline pipeline, Integer identId);

  Optional<PadPipelineIdent> getByPadPipelineAndAndIdentNo(PadPipeline pipeline, Integer identNo);

  @EntityGraph(attributePaths = "padPipeline")
  List<PadPipelineIdent> getAllByPadPipeline_IdIn(List<Integer> padPipelineIds);

}
