package uk.co.ogauthority.pwa.features.application.tasks.othertechprops;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadPipelineOtherPropertiesRepository extends CrudRepository<PadPipelineOtherProperties, Integer> {

  List<PadPipelineOtherProperties> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
