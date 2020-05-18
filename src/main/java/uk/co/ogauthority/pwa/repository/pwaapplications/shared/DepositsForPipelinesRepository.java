package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipelines;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


public interface DepositsForPipelinesRepository extends CrudRepository<PadDepositPipelines, Integer> {

  List<PadPermanentDeposit> findAllPermanentDepositInformationByPadPipelineId(Integer padPipelineId);

}