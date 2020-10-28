package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


public interface PadDepositPipelineRepository extends CrudRepository<PadDepositPipeline, Integer> {


  @EntityGraph(attributePaths = {"pipeline"})
  List<PadDepositPipeline> findAllByPadPermanentDeposit(PadPermanentDeposit padPermanentDeposit);

  List<PadDepositPipeline> getAllByPipeline(Pipeline pipeline);

  List<PadDepositPipeline> getAllByPipeline_MasterPwa(MasterPwa masterPwa);

  List<PadDepositPipeline> getAllByPadPermanentDeposit_PwaApplicationDetail(PwaApplicationDetail detail);

}
