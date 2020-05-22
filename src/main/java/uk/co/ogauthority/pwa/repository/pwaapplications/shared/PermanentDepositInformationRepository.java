package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


public interface PermanentDepositInformationRepository extends CrudRepository<PadPermanentDeposit, Integer> {

  List<PadPermanentDeposit> findByPwaApplicationDetailOrderByReferenceAsc(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadPermanentDeposit> findByReference(String reference);
}
