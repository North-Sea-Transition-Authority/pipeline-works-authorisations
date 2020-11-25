package uk.co.ogauthority.pwa.repository.appprocessing.options;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApplicationApproval;
import uk.co.ogauthority.pwa.model.entity.appprocessing.options.OptionsApprovalDeadlineHistory;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;

public interface OptionsApprovalDeadlineHistoryRepository extends CrudRepository<OptionsApprovalDeadlineHistory, Integer> {

  Optional<OptionsApprovalDeadlineHistory> findByOptionsApplicationApproval_PwaApplicationAndTipFlagIsTrue(
      PwaApplication pwaApplication
  );

  Optional<OptionsApprovalDeadlineHistory> findByOptionsApplicationApprovalAndTipFlagIsTrue(
      OptionsApplicationApproval optionsApplicationApproval
  );

}