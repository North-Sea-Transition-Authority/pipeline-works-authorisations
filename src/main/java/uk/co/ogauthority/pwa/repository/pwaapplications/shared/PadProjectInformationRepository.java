package uk.co.ogauthority.pwa.repository.pwaapplications.shared;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;

public interface PadProjectInformationRepository extends CrudRepository<PadProjectInformation, Integer> {

  Optional<PadProjectInformation> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
