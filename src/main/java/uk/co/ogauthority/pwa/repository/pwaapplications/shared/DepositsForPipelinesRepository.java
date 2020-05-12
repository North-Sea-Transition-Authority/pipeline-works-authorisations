package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.DepositsForPipelines;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;

import java.util.List;

public interface DepositsForPipelinesRepository extends CrudRepository<DepositsForPipelines, Integer> {

    List<DepositsForPipelines> findAllByPadProjectInfoIdId(Integer padProjectInfoId);
    List<PermanentDepositInformation> findAllPermanentDepositInformationByPadPipelineId(Integer padPipelineId);

}