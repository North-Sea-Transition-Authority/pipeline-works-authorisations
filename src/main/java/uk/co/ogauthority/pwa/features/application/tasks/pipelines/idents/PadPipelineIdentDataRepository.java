package uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadPipelineIdentDataRepository extends CrudRepository<PadPipelineIdentData, Integer> {

  @EntityGraph(attributePaths = { "padPipelineIdent.padPipeline.pipeline" })
  List<PadPipelineIdentData> getAllByPadPipelineIdentIn(List<PadPipelineIdent> idents);

  @EntityGraph(attributePaths = { "padPipelineIdent.padPipeline.pipeline" })
  Optional<PadPipelineIdentData> getByPadPipelineIdent(PadPipelineIdent ident);

  @EntityGraph(attributePaths = { "padPipelineIdent.padPipeline.pipeline" })
  List<PadPipelineIdentData> getAllByPadPipelineIdent_PadPipeline(PadPipeline padPipeline);

  @Query("" +
      "SELECT ppid " +
      "FROM PadPipelineIdentData ppid " +
      "JOIN PadPipelineIdent ppi ON ppid.padPipelineIdent = ppi " +
      "JOIN PadPipeline pp ON ppi.padPipeline = pp " +
      "JOIN PwaApplicationDetail pad ON pp.pwaApplicationDetail = pad " +
      "WHERE pad = :pwaApplicationDetail")
  List<PadPipelineIdentData> getAllPadPipelineIdentDataByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  @EntityGraph(attributePaths = { "padPipelineIdent.padPipeline", "padPipelineIdent.padPipeline.pipeline" })
  List<PadPipelineIdentData> getAllByPadPipelineIdent_PadPipelineIn(Collection<PadPipeline> padPipelines);

}
