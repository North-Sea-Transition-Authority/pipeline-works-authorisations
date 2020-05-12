package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.DepositsForPipelines;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;


public interface DepositsForPipelinesRepository extends CrudRepository<DepositsForPipelines, Integer> {

  List<PermanentDepositInformation> findAllPermanentDepositInformationByPadPipelineId(Integer padPipelineId);

}