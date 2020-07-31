package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


public interface PadDepositPipelineRepository extends CrudRepository<PadDepositPipeline, Integer> {


  @EntityGraph(attributePaths = {"padPipeline"})
  List<PadDepositPipeline> findAllByPadPermanentDeposit(PadPermanentDeposit padPermanentDeposit);

}
