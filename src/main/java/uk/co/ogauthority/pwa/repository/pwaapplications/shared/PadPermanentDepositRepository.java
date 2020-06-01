package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;


public interface PadPermanentDepositRepository extends CrudRepository<PadPermanentDeposit, Integer> {

  long countByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PadPermanentDeposit> findByPwaApplicationDetailOrderByReferenceAsc(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadPermanentDeposit> findByPwaApplicationDetailAndReferenceIgnoreCase(
      PwaApplicationDetail pwaApplicationDetail, String reference);
}
