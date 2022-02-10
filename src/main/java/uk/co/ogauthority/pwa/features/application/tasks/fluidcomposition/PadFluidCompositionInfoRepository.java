package uk.co.ogauthority.pwa.features.application.tasks.fluidcomposition;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;


public interface PadFluidCompositionInfoRepository extends CrudRepository<PadFluidCompositionInfo, Integer> {

  List<PadFluidCompositionInfo> getAllByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);
}
