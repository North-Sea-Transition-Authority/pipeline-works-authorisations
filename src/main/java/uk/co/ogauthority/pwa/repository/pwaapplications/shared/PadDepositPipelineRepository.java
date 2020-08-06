package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;


public interface PadDepositPipelineRepository extends CrudRepository<PadDepositPipeline, Integer> {


  List<PadDepositPipeline> findAllByPermanentDepositInfoId(Integer permanentDepositInfoId);

  List<PadDepositPipeline> getAllByPadPipeline(PadPipeline padPipeline);

  List<PadDepositPipeline> getAllByPadPipeline_PwaApplicationDetail(PwaApplicationDetail detail);

}
