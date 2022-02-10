package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PadProjectInformationRepository extends CrudRepository<PadProjectInformation, Integer> {

  Optional<PadProjectInformation> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
