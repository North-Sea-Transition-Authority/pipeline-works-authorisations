package uk.co.ogauthority.pwa.repository.pipelines;

import java.util.List;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public interface PipelineDetailDtoRepository {

  List<PipelineBundlePairDto> getBundleNamesByPwaApplicationDetail(PwaApplicationDetail pwaApplicationDetail);

}
