package uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Interface used to enhance the default repository so DTOs can be produced easily.
 */
public interface PadPipelineDtoRepository {

  List<PadPipelineOverviewDto> findAllAsOverviewDtoByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

  Long countAllWithNoIdentsByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
