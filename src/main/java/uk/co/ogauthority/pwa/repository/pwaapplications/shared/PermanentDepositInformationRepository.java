package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;

public interface PermanentDepositInformationRepository extends CrudRepository<PermanentDepositInformation, Integer> {

}
