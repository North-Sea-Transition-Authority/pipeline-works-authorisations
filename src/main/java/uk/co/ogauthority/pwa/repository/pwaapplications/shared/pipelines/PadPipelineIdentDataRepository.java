package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;

public interface PadPipelineIdentDataRepository extends CrudRepository<PadPipelineIdentData, Integer> {

  @EntityGraph(attributePaths = { "padPipelineIdent" })
  List<PadPipelineIdentData> getAllByPadPipelineIdentIn(List<PadPipelineIdent> idents);

  @EntityGraph(attributePaths = { "padPipelineIdent" })
  Optional<PadPipelineIdentData> getByPadPipelineIdent(PadPipelineIdent ident);

  @EntityGraph(attributePaths = { "padPipelineIdent" })
  List<PadPipelineIdentData> getAllByPadPipelineIdent_PadPipeline(PadPipeline padPipeline);

  @Query("" +
      "SELECT ppid " +
      "FROM PadPipelineIdentData ppid " +
      "JOIN PadPipelineIdent ppi ON ppid.padPipelineIdent = ppi " +
      "JOIN PadPipeline pp ON ppi.padPipeline = pp " +
      "JOIN PwaApplicationDetail pad ON pp.pwaApplicationDetail = pad " +
      "WHERE pad = :pwaApplicationDetail")
  List<PadPipelineIdentData> getAllPadPipelineIdentDataByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
