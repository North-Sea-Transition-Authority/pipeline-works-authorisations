package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadDepositPipelineRepository extends CrudRepository<PadDepositPipeline, Integer> {


  @EntityGraph(attributePaths = {"pipeline"})
  List<PadDepositPipeline> findAllByPadPermanentDeposit(PadPermanentDeposit padPermanentDeposit);

  List<PadDepositPipeline> getAllByPipeline(Pipeline pipeline);

  List<PadDepositPipeline> getAllByPadPermanentDeposit_PwaApplicationDetail(PwaApplicationDetail detail);

  List<PadDepositPipeline> getAllByPadPermanentDeposit_PwaApplicationDetailAndPipeline(
      PwaApplicationDetail pwaApplicationDetail, Pipeline pipeline);

  Long countAllByPadPermanentDeposit(PadPermanentDeposit padPermanentDeposit);

}
