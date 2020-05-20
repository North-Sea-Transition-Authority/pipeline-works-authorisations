package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipelines;


public interface DepositsForPipelinesRepository extends CrudRepository<PadDepositPipelines, Integer> {


  List<PadDepositPipelines> findAllByPermanentDepositInfoId(Integer permanentDepositInfoId);

}
