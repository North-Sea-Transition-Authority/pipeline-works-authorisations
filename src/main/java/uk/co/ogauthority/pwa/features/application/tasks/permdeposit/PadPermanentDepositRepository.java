package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadPermanentDepositRepository extends CrudRepository<PadPermanentDeposit, Integer> {

  long countByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  List<PadPermanentDeposit> findByPwaApplicationDetailOrderByReferenceAsc(PwaApplicationDetail pwaApplicationDetail);

  Optional<PadPermanentDeposit> findByPwaApplicationDetailAndReferenceIgnoreCase(
      PwaApplicationDetail pwaApplicationDetail, String reference);

  List<PadPermanentDeposit> getAllByPwaApplicationDetail(PwaApplicationDetail detail);

  List<PadPermanentDeposit> getAllByPwaApplicationDetailAndDepositIsForPipelinesOnOtherApp(
      PwaApplicationDetail detail,
      Boolean depositIsForPipelinesOnOtherApp);
}
