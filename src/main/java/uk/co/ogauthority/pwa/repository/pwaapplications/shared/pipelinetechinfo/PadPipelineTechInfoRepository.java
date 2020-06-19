package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineTechInfo;


public interface PadPipelineTechInfoRepository extends CrudRepository<PadPipelineTechInfo, Integer> {

  Optional<PadPipelineTechInfo> findByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
