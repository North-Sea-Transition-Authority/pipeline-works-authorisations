package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;

@Repository
public interface PadMedianLineAgreementRepository extends CrudRepository<PadMedianLineAgreement, Integer> {

  Optional<PadMedianLineAgreement> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  int countAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
