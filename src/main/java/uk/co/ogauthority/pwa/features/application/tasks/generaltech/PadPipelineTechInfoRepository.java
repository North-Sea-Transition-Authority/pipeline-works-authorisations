package uk.co.ogauthority.pwa.features.application.tasks.generaltech;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadPipelineTechInfoRepository extends CrudRepository<PadPipelineTechInfo, Integer> {

  Optional<PadPipelineTechInfo> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
