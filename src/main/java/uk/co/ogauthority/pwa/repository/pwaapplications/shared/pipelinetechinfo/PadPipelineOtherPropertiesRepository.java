package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelinetechinfo;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinetechinfo.PadPipelineOtherProperties;


public interface PadPipelineOtherPropertiesRepository extends CrudRepository<PadPipelineOtherProperties, Integer> {

  List<PadPipelineOtherProperties> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
